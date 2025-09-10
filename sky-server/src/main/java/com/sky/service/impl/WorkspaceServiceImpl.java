package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.User;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.11 00:34
 */

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据时间段统计营业数据 (优化后)
     * 优化点: 将3次数据库查询减少为2次，并在内存中通过流处理计算指标。
     */
    @Override
    public BusinessDataVO getBusinessData() {
        // 0. 获取当天的开始和结束时间
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        // 1. 一次性查询出时间范围内的所有订单
        List<Orders> ordersList = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getOrderTime, begin)
                .le(Orders::getOrderTime, end));

        // 2. 在内存中筛选出已完成的订单
        List<Orders> completedOrders = ordersList.stream()
                .filter(order -> Orders.COMPLETED.equals(order.getStatus()))
                .toList();

        // 3. 通过流处理计算营业额、有效订单数、总订单数
        BigDecimal turnover = completedOrders.stream()
                .map(Orders::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalOrderCount = ordersList.size();
        int validOrderCount = completedOrders.size();

        // 4. 计算订单完成率和平均客单价，并处理除零异常
        double orderCompletionRate = 0.0;
        if (totalOrderCount > 0) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        double unitPrice = 0.0;
        if (validOrderCount > 0) {
            unitPrice = turnover.divide(new BigDecimal(validOrderCount), 2, RoundingMode.HALF_UP).doubleValue();
        }

        // 5. 单独查询新增用户数
        Long newUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, begin)
                .le(User::getCreateTime, end));

        return BusinessDataVO.builder()
                .turnover(turnover.doubleValue())
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers.intValue())
                .build();
    }

    /**
     * 查询订单管理数据 (优化后)
     * 优化点: 将5次数据库查询减少为1次，通过流处理分组计算。
     */
    @Override
    public OrderOverViewVO getOrderOverView() {
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);

        // 1. 一次性查询出今天的所有订单
        List<Orders> ordersList = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getOrderTime, begin));

        // 2. 通过流处理按状态分组并计数
        Map<Integer, Long> orderStatusMap = ordersList.stream()
                .collect(Collectors.groupingBy(Orders::getStatus, Collectors.counting()));

        // 3. 从Map中安全地获取各个状态的订单数量
        Integer waitingOrders = orderStatusMap.getOrDefault(Orders.TO_BE_CONFIRMED, 0L).intValue();
        Integer deliveredOrders = orderStatusMap.getOrDefault(Orders.CONFIRMED, 0L).intValue();
        Integer completedOrders = orderStatusMap.getOrDefault(Orders.COMPLETED, 0L).intValue();
        Integer cancelledOrders = orderStatusMap.getOrDefault(Orders.CANCELLED, 0L).intValue();
        Integer allOrders = ordersList.size();

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     */
    @Override
    public DishOverViewVO getDishOverView() {
        Long sold = dishMapper.selectCount(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, StatusConstant.ENABLE));
        Long discontinued = dishMapper.selectCount(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, StatusConstant.DISABLE));

        return DishOverViewVO.builder()
                .sold(sold.intValue())
                .discontinued(discontinued.intValue())
                .build();
    }

    /**
     * 查询套餐总览
     */
    @Override
    public SetmealOverViewVO getSetmealOverView() {
        Long sold = setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getStatus, StatusConstant.ENABLE));
        Long discontinued = setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getStatus, StatusConstant.DISABLE));

        return SetmealOverViewVO.builder()
                .sold(sold.intValue())
                .discontinued(discontinued.intValue())
                .build();
    }
}
