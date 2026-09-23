package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     *
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号和用户id查询订单
     *
     * @param orderNumber
     * @param userId
     * @return
     */
    @Select("select * from orders where number = #{orderNumber} and user_id = #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 根据订单号查询订单
     *
     * @param number
     * @return
     */
    @Select("select * from orders where number = #{number}")
    Orders getByNumber(String number);

    /**
     * 根据订单号统计订单数量（生成订单号时查重，避免重复）
     *
     * @param number
     * @return
     */
    @Select("select count(id) from orders where number = #{number}")
    Integer countByNumber(String number);

    /**
     * 根据id查询订单
     *
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据id动态修改订单信息（状态、支付状态、结账时间等）
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页条件查询并按下单时间排序（用户端/管理端通用）
     *
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据状态统计某段时间内订单的数量或金额（countByMap/sumByMap 配套）
     *
     * @param map begin、end、status
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据动态条件统计订单数量
     *
     * @param map begin、end、status
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 按日期分组统计营业数据（订单总数/有效订单数/营业额）
     * 一次查询覆盖整个区间，替代「按天循环查询」，避免 30 天区间产生上百次 SQL
     *
     * @param begin 区间开始
     * @param end   区间结束
     * @return 每行包含 orderDate、orderCount、validOrderCount、turnover
     */
    List<Map<String, Object>> getDailyBusinessData(@Param("begin") LocalDateTime begin,
                                                   @Param("end") LocalDateTime end);

    /**
     * 统计指定时间区间内的销量排名前10（菜品/套餐按名称聚合）
     *
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);

    /**
     * 根据状态统计订单数量
     *
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 根据订单状态和下单时间查询订单（超时订单处理用）
     *
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

}
