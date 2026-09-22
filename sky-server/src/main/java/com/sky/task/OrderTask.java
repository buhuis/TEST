package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单状态定时处理任务
 * 1. 每分钟检查一次：下单超过15分钟仍未支付的订单自动取消
 * 2. 每天凌晨1点：一直处于"派送中"超过1小时的订单自动置为已完成
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理支付超时订单：每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ?") //每分钟
    public void processTimeoutOrder() {
        log.info("定时任务：处理支付超时订单 {}", LocalDateTime.now());

        //查询"待付款"且下单时间超过15分钟的订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));

        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时未支付，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
            log.info("已自动取消 {} 笔超时未支付订单", ordersList.size());
        }
    }

    /**
     * 处理派送中状态异常的订单：每天凌晨1点触发一次
     */
    @Scheduled(cron = "0 0 1 * * ?") //每天凌晨1点
    public void processDeliveryOrder() {
        log.info("定时任务：处理派送中超时订单 {}", LocalDateTime.now());

        //查询"派送中"且下单时间超过1小时的订单，置为已完成
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));

        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orders.setDeliveryTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
            log.info("已将 {} 笔派送中超时订单置为已完成", ordersList.size());
        }
    }

}
