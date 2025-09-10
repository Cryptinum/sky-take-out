package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.10 14:45
 */

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        String dateStrList = getDateStrList(dateList);

        List<Orders> ordersList = getCompletedOrdersByDateRange(begin, end);

        // 转换为以日期为键，营业额为值的Map
        Map<LocalDate, BigDecimal> turnoverMap = ordersList.stream().collect(
                Collectors.groupingBy(
                        order -> order.getOrderTime().toLocalDate(),
                        Collectors.mapping(
                                Orders::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                )
        );

        // 如果某日期缺少订单则赋0
        String turnoverStrList = dateList.stream()
                .map(date -> turnoverMap.getOrDefault(date, BigDecimal.ZERO).toString())
                .collect(Collectors.joining(","));

        return TurnoverReportVO.builder()
                .dateList(dateStrList)
                .turnoverList(turnoverStrList)
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        String dateStrList = getDateStrList(dateList);
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);

        // 查询指定日期范围内每天的新用户数
        Map<LocalDate, Long> newUserMap = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .ge(User::getCreateTime, beginTime)
                        .le(User::getCreateTime, endTime))
                .stream()
                .collect(Collectors.groupingBy(
                        user -> user.getCreateTime().toLocalDate(),
                        Collectors.counting()
                ));

        // 计算从开始日期前一天为止的总用户数
        Long totalUserBeforeBegin = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .lt(User::getCreateTime, beginTime));

        // 计算每天的累计用户数，依次累加到totalUserBeforeBegin上
        List<Long> totalUserList = new ArrayList<>();
        Long runningTotal = totalUserBeforeBegin;
        for (LocalDate date : dateList) {
            runningTotal += newUserMap.getOrDefault(date, 0L);
            totalUserList.add(runningTotal);
        }

        // 格式转换
        String newUserStrList = dateList.stream()
                .map(date -> newUserMap.getOrDefault(date, 0L).toString())
                .collect(Collectors.joining(","));
        String totalUserStrList = totalUserList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return UserReportVO.builder()
                .dateList(dateStrList)
                .newUserList(newUserStrList)
                .totalUserList(totalUserStrList)
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        String dateStrList = getDateStrList(dateList);
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);

        List<Orders> ordersList = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getOrderTime, beginTime)
                .le(Orders::getOrderTime, endTime));

        // 分别统计每天的订单总数和有效订单数（已完成订单）
        Map<LocalDate, Long> dailyOrderCountMap = ordersList.stream()
                .collect(Collectors.groupingBy(order -> order.getOrderTime().toLocalDate(), Collectors.counting()));

        Map<LocalDate, Long> dailyValidOrderCountMap = ordersList.stream()
                .filter(order -> Orders.COMPLETED.equals(order.getStatus()))
                .collect(Collectors.groupingBy(order -> order.getOrderTime().toLocalDate(), Collectors.counting()));

        Integer totalOrderCount = ordersList.size();
        Integer validOrderCount = dailyValidOrderCountMap.values().stream().mapToInt(Long::intValue).sum();
        Double orderCompletionRate = (totalOrderCount == 0) ? 0.0 : validOrderCount.doubleValue() / totalOrderCount;

        // 格式转换
        String orderCountStrList = dateList.stream()
                .map(date -> dailyOrderCountMap.getOrDefault(date, 0L).toString())
                .collect(Collectors.joining(","));
        String validOrderCountStrList = dateList.stream()
                .map(date -> dailyValidOrderCountMap.getOrDefault(date, 0L).toString())
                .collect(Collectors.joining(","));

        return OrderReportVO.builder()
                .dateList(dateStrList)
                .orderCountList(orderCountStrList)
                .validOrderCountList(validOrderCountStrList)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    @Transactional
    public SalesTop10ReportVO top10Statistics(LocalDate begin, LocalDate end) {
        List<Orders> completedOrders = getCompletedOrdersByDateRange(begin, end);
        List<Long> completedOrderIds = completedOrders.stream().map(Orders::getId).collect(Collectors.toList());

        if (completedOrderIds.isEmpty()) {
            return SalesTop10ReportVO.builder().nameList("").numberList("").build();
        }

        // 查询所有已完成订单的订单明细
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .in(OrderDetail::getOrderId, completedOrderIds));

        Map<String, Integer> salesMap = orderDetailList.stream()
                .collect(Collectors.groupingBy(
                        OrderDetail::getName,
                        Collectors.summingInt(OrderDetail::getNumber)
                ));

        List<Map.Entry<String, Integer>> sortedSalesList = salesMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .toList();

        // 格式转换
        String nameList = sortedSalesList.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(","));
        String numberList = sortedSalesList.stream()
                .map(entry -> entry.getValue().toString())
                .collect(Collectors.joining(","));

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    // ================== Private Helper Methods ==================

    /**
     * 根据开始和结束日期，生成一个连续的日期列表
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return List<LocalDate>
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        return Stream
                .iterate(begin, date -> date.plusDays(1))
                .limit(begin.until(end).getDays() + 1)
                .toList();
    }

    /**
     * 将日期列表转换为逗号分隔的字符串
     *
     * @param dateList 日期列表
     * @return 逗号分隔的日期字符串
     */
    private String getDateStrList(List<LocalDate> dateList) {
        return dateList.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(","));
    }

    /**
     * 根据日期范围查询已完成的订单列表
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return List<Orders>
     */
    private List<Orders> getCompletedOrdersByDateRange(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);
        return ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getOrderTime, beginTime)
                .le(Orders::getOrderTime, endTime)
                .eq(Orders::getStatus, Orders.COMPLETED)
        );
    }
}