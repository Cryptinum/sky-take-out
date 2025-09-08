package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.08 16:15
 */

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrdersMapper ordersMapper;

    public OrderTask(OrdersMapper ordersMapper) {
        this.ordersMapper = ordersMapper;
    }

    @Scheduled(cron = "0 * * * * *")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单: {}", LocalDateTime.now());

        // 先查询有没有超时订单
        // 查询的条件是status为1（待支付）并且order_time在当前时间之前15分钟的订单
        List<Orders> ordersList = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, Orders.PENDING_PAYMENT)
                .lt(Orders::getOrderTime, LocalDateTime.now().minusMinutes(15)));

        if (ordersList == null || ordersList.isEmpty()) {
            return;
        }

        for (Orders orders : ordersList) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelTime(LocalDateTime.now());
            orders.setCancelReason("订单超时，系统自动取消");
        }

        ordersMapper.updateById(ordersList);
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrders() {
        log.info("定时处理派送中的订单: {}", LocalDateTime.now());

        List<Orders> ordersList = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS)
                .lt(Orders::getOrderTime, LocalDateTime.now().minusHours(1)));

        if (ordersList == null || ordersList.isEmpty()) {
            return;
        }

        for (Orders orders : ordersList) {
            orders.setStatus(Orders.COMPLETED);
            orders.setDeliveryTime(LocalDateTime.now());
        }

        ordersMapper.updateById(ordersList);
    }
}
