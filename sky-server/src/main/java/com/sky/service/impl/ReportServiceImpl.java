package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
}
