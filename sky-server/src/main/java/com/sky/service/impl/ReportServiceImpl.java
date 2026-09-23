package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.exception.BaseException;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定时间区间内的营业额数据（营业额 = 状态为"已完成"的订单金额合计）
     * 优化：由「按天循环查库」改为「一次分组查询 + 内存填充」
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //存放从 begin 到 end 范围内每天的日期
        List<LocalDate> dateList = getDateList(begin, end);

        //一次查询拿到区间内按天分组的营业数据，避免 30 天区间执行 30 次 sum 查询
        Map<String, Map<String, Object>> dailyMap = queryDailyBusinessData(begin, end);

        //存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            turnoverList.add(getDouble(dailyMap.get(date.toString()), "turnover"));
        }

        //封装返回结果
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 统计指定时间区间内的用户数据（每日新增用户 / 用户总量）
     * 优化：新增用户一次分组查询；用户总量改为「区间前存量 + 逐日累加」，语义不变但省去 2n 次查询
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        //1. 区间开始前的存量用户，作为累计总量的基数
        Map<String, Object> baseMap = new HashMap<>();
        baseMap.put("end", LocalDateTime.of(begin, LocalTime.MIN));
        Integer baseUserCount = userMapper.countByMap(baseMap);
        int totalUser = baseUserCount == null ? 0 : baseUserCount;

        //2. 一次查询拿到区间内每天的新增用户数
        Map<String, Integer> newUserMap = queryDailyNewUser(begin, end);

        //存放每天的新增用户数量 / 每天的用户总量
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            int newUser = newUserMap.getOrDefault(date.toString(), 0);
            totalUser += newUser;
            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 统计指定时间区间内的订单数据（每日订单数 / 每日有效订单数 / 订单完成率）
     * 优化：由「每天 2 次循环查库」改为「一次分组查询 + 内存填充」
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        Map<String, Map<String, Object>> dailyMap = queryDailyBusinessData(begin, end);

        //存放每天的订单总数 / 每天的有效订单数
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        int totalOrderCount = 0;
        int validOrderCount = 0;
        for (LocalDate date : dateList) {
            Map<String, Object> row = dailyMap.get(date.toString());
            int orderCount = getInt(row, "orderCount");
            int validCount = getInt(row, "validOrderCount");

            orderCountList.add(orderCount);
            validOrderCountList.add(validCount);

            totalOrderCount += orderCount;
            validOrderCount += validCount;
        }

        //订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 统计指定时间区间内的销量排名前10（已完成订单的明细按商品名聚合）
     *
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出运营数据报表：查询最近30天的运营数据，POI 基于模板填充后下载
     * 优化：原来逐日调用 4 次统计查询（30天 ≈ 120 次 SQL），现改为 2 次分组查询后内存汇总
     *
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        List<LocalDate> dateList = getDateList(dateBegin, dateEnd);

        //2. 两次查询拿到全部明细：订单营业数据 + 新增用户数
        Map<String, Map<String, Object>> dailyMap = queryDailyBusinessData(dateBegin, dateEnd);
        Map<String, Integer> newUserMap = queryDailyNewUser(dateBegin, dateEnd);

        //3. 由每日明细在内存中汇总出概览数据，保证概览与明细口径完全一致
        double totalTurnover = 0.0;
        int totalOrderCount = 0;
        int totalValidOrderCount = 0;
        int totalNewUsers = 0;
        for (LocalDate date : dateList) {
            Map<String, Object> row = dailyMap.get(date.toString());
            totalTurnover += getDouble(row, "turnover");
            totalOrderCount += getInt(row, "orderCount");
            totalValidOrderCount += getInt(row, "validOrderCount");
            totalNewUsers += newUserMap.getOrDefault(date.toString(), 0);
        }
        double totalCompletionRate = totalOrderCount == 0 ? 0.0 : (double) totalValidOrderCount / totalOrderCount;
        double totalUnitPrice = totalValidOrderCount == 0 ? 0.0 : totalTurnover / totalValidOrderCount;

        //4. 通过 POI 将数据写入到 Excel 文件中（模板来自 resources/template）
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        if (in == null) {
            log.error("运营数据报表模板不存在：resources/template/运营数据报表模板.xlsx");
            throw new BaseException("运营数据报表模板不存在，无法导出");
        }

        //设置下载响应头，浏览器访问时直接下载为带日期的文件名
        String fileName = "运营数据报表_" + dateBegin + "_" + dateEnd + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        try {
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            //编码异常不影响文件内容，忽略即可
            log.warn("报表文件名编码失败：{}", e.getMessage());
        }

        //try-with-resources 保证异常时模板流、工作簿、输出流都被正确关闭
        try (InputStream templateIn = in;
             XSSFWorkbook excel = new XSSFWorkbook(templateIn)) {

            //获取表格文件的 Sheet 页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + " 至 " + dateEnd);

            //获得第4行（概览区）
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(BigDecimal.valueOf(totalTurnover).setScale(2, RoundingMode.HALF_UP).doubleValue());
            row.getCell(4).setCellValue(totalCompletionRate);
            row.getCell(6).setCellValue(totalNewUsers);

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(totalValidOrderCount);
            row.getCell(4).setCellValue(BigDecimal.valueOf(totalUnitPrice).setScale(2, RoundingMode.HALF_UP).doubleValue());

            //填充明细数据
            for (int i = 0; i < dateList.size(); i++) {
                LocalDate date = dateList.get(i);
                Map<String, Object> dayData = dailyMap.get(date.toString());

                double dayTurnover = getDouble(dayData, "turnover");
                int dayOrderCount = getInt(dayData, "orderCount");
                int dayValidOrderCount = getInt(dayData, "validOrderCount");
                int dayNewUsers = newUserMap.getOrDefault(date.toString(), 0);

                double dayCompletionRate = dayOrderCount == 0 ? 0.0 : (double) dayValidOrderCount / dayOrderCount;
                double dayUnitPrice = dayValidOrderCount == 0 ? 0.0 : dayTurnover / dayValidOrderCount;

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dayTurnover);
                row.getCell(3).setCellValue(dayValidOrderCount);
                row.getCell(4).setCellValue(dayCompletionRate);
                row.getCell(5).setCellValue(dayUnitPrice);
                row.getCell(6).setCellValue(dayNewUsers);
            }

            //5. 通过输出流将 Excel 文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.flush();
        } catch (IOException e) {
            log.error("导出运营数据报表失败：{}", e.getMessage());
            throw new BaseException("导出运营数据报表失败：" + e.getMessage());
        }
    }

    /**
     * 一次性查询区间内按天分组的订单营业数据，返回「日期 -> 当天数据」的映射
     * key 形如 2026-09-01，与 LocalDate.toString() 保持一致
     *
     * @param begin
     * @param end
     * @return
     */
    private Map<String, Map<String, Object>> queryDailyBusinessData(LocalDate begin, LocalDate end) {
        List<Map<String, Object>> rows = orderMapper.getDailyBusinessData(
                LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        Map<String, Map<String, Object>> dailyMap = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                dailyMap.put(String.valueOf(row.get("orderDate")), row);
            }
        }
        return dailyMap;
    }

    /**
     * 一次性查询区间内按天分组的新增用户数，返回「日期 -> 新增用户数」的映射
     *
     * @param begin
     * @param end
     * @return
     */
    private Map<String, Integer> queryDailyNewUser(LocalDate begin, LocalDate end) {
        List<Map<String, Object>> rows = userMapper.countNewUserByDate(
                LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        Map<String, Integer> newUserMap = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                newUserMap.put(String.valueOf(row.get("orderDate")), ((Number) row.get("newUsers")).intValue());
            }
        }
        return newUserMap;
    }

    /**
     * 从分组查询结果中安全取出数值（该日期无数据时返回 0）
     */
    private double getDouble(Map<String, Object> row, String key) {
        if (row == null || row.get(key) == null) {
            return 0.0;
        }
        return ((Number) row.get(key)).doubleValue();
    }

    /**
     * 从分组查询结果中安全取出整型数值（该日期无数据时返回 0）
     */
    private int getInt(Map<String, Object> row, String key) {
        if (row == null || row.get(key) == null) {
            return 0;
        }
        return ((Number) row.get(key)).intValue();
    }

    /**
     * 构造 begin 到 end 之间每天的日期列表（含首尾）
     * 注意：必须保证 begin 不晚于 end，否则日期永远不会相等会造成死循环
     *
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        if (begin == null || end == null) {
            throw new BaseException("统计的开始日期和结束日期不能为空");
        }
        if (begin.isAfter(end)) {
            throw new BaseException("统计的开始日期不能晚于结束日期");
        }
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = begin;
        dateList.add(current);
        while (!current.equals(end)) {
            current = current.plusDays(1);
            dateList.add(current);
        }
        return dateList;
    }
}
