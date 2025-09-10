package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        // --- 步骤 1: 查询并计算出实际有营业额的日期和金额 ---

        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);

        List<Orders> ordersList = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getOrderTime, beginTime)
                .le(Orders::getOrderTime, endTime)
                .eq(Orders::getStatus, Orders.COMPLETED)
        );

        Map<LocalDate, BigDecimal> turnoverMap = ordersList.stream().collect(
                Collectors.groupingBy(
                        order -> order.getOrderTime().toLocalDate(),
                        Collectors.mapping(
                                Orders::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                )
        );

        // --- 步骤 2: 生成从 begin 到 end 的完整连续日期列表 ---

        // 使用 Stream.iterate 生成日期流，从 begin 开始，每天加1，直到 end 日期
        List<LocalDate> dateList = Stream
                .iterate(begin, date -> date.plusDays(1))
                .limit(begin.until(end).getDays() + 1)
                .toList();

        // --- 步骤 3: 遍历完整日期列表，从 Map 中获取营业额，如果不存在则用 0 填充 ---

        // 拼接日期字符串，例如 "2025-09-08,2025-09-09,2025-09-10"
        String dateStrList = dateList.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(","));

        // 拼接每日营业额字符串，例如 "50.00,0,98.00"
        String turnoverStrList = dateList.stream()
                // 使用 Map.getOrDefault 方法，如果 map 中没有这个 date key，就返回默认值 BigDecimal.ZERO
                .map(date -> turnoverMap.getOrDefault(date, BigDecimal.ZERO))
                .map(BigDecimal::toString)
                .collect(Collectors.joining(","));

        // --- 步骤 4: 封装 TurnoverReportVO 返回结果 ---
        return TurnoverReportVO.builder()
                .dateList(dateStrList)
                .turnoverList(turnoverStrList)
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        // 思路：先统计begin之前的，然后在后端逐天累加得到数据

        // 1. 生成从 begin 到 end 的完整连续日期列表
        List<LocalDate> dateList = Stream.iterate(begin, date -> date.plusDays(1))
                .limit(begin.until(end).getDays() + 1)
                .toList();

        // 2. 查询并计算每日新增用户数
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);

        List<User> userList = userMapper.selectList(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, beginTime)
                .le(User::getCreateTime, endTime)
        );

        Map<LocalDate, Long> newUserMap = userList.stream()
                .collect(Collectors.groupingBy(
                        user -> user.getCreateTime().toLocalDate(),
                        Collectors.counting()
                ));

        // 3. 计算每日总用户数
        // 3.1 查询起始日期前的总用户数
        Long totalUserBeforeBegin = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .lt(User::getCreateTime, beginTime)
        );

        List<Long> totalUserList = new ArrayList<>();
        Long runningTotal = totalUserBeforeBegin;

        for (LocalDate date : dateList) {
            // 获取当天的增量
            Long newUserCount = newUserMap.getOrDefault(date, 0L);
            // 累加到运行总数上
            runningTotal += newUserCount;
            totalUserList.add(runningTotal);
        }

        // 4. 整理并拼接成VO所需的字符串格式
        String dateStrList = dateList.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(","));

        String newUserStrList = dateList.stream()
                .map(date -> newUserMap.getOrDefault(date, 0L).toString())
                .collect(Collectors.joining(","));

        String totalUserStrList = totalUserList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // 5. 封装结果并返回
        return UserReportVO.builder()
                .dateList(dateStrList)
                .newUserList(newUserStrList)
                .totalUserList(totalUserStrList)
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        // 1. 生成从 begin 到 end 的完整连续日期列表
        List<LocalDate> dateList = Stream.iterate(begin, date -> date.plusDays(1))
                .limit(begin.until(end).getDays() + 1)
                .toList();

        // 2. 查询指定日期范围内的所有订单
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);

        List<Orders> ordersList = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getOrderTime, beginTime)
                .le(Orders::getOrderTime, endTime)
        );

        // 3. 计算每日总订单数
        Map<LocalDate, Long> dailyOrderCountMap = ordersList.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderTime().toLocalDate(),
                        Collectors.counting()
                ));

        // 4. 计算每日有效订单数
        Map<LocalDate, Long> dailyValidOrderCountMap = ordersList.stream()
                .filter(order -> Orders.COMPLETED.equals(order.getStatus())) // 筛选出已完成订单
                .collect(Collectors.groupingBy(
                        order -> order.getOrderTime().toLocalDate(),
                        Collectors.counting()
                ));

        // 5. 计算区间内的总订单数
        Integer totalOrderCount = ordersList.size();

        // 6. 计算区间内的总有效订单数
        Integer validOrderCount = dailyValidOrderCountMap.values().stream()
                .mapToInt(Long::intValue)
                .sum();

        // 7. 计算订单完成率
        Double orderCompletionRate = (totalOrderCount == 0) ? 0.0 : validOrderCount.doubleValue() / totalOrderCount;

        // 8. 拼接每日订单数字符串
        String orderCountStrList = dateList.stream()
                .map(date -> dailyOrderCountMap.getOrDefault(date, 0L).toString())
                .collect(Collectors.joining(","));

        // 9. 拼接每日有效订单数字符串
        String validOrderCountStrList = dateList.stream()
                .map(date -> dailyValidOrderCountMap.getOrDefault(date, 0L).toString())
                .collect(Collectors.joining(","));

        // 10. 封装结果并返回
        return OrderReportVO.builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .orderCountList(orderCountStrList)
                .validOrderCountList(validOrderCountStrList)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
}
