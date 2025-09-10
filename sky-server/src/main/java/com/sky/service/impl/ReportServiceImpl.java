package com.sky.service.impl;

import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

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

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        try {
            // 这里我们选择调用 Hutool 的实现，因为它更简洁
            exportWithHutool(response);
            // 如果想使用原生POI，可以调用 exportWithPOI(response);
        } catch (IOException e) {
            log.error("导出运营数据报表失败", e);
        }
    }

    /**
     * 使用 Hutool 实现导出报表
     * @param response
     * @throws IOException
     */
    private void exportWithHutool(HttpServletResponse response) throws IOException {
        // 1. 获取近30天的业务数据
        LocalDate beginDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(beginDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        TurnoverReportVO turnoverData = turnoverStatistics(beginDate, endDate);
        OrderReportVO orderData = ordersStatistics(beginDate, endDate);
        UserReportVO userData = userStatistics(beginDate, endDate);

        // 2. 加载模板文件
        InputStream templateStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(templateStream);
        ExcelWriter writer = new ExcelWriter(workbook.getSheet("Sheet1"));
        // 明确要操作的sheet，Hutool默认为"Sheet1"或第一个
        writer.setSheet(0);

        // 3. 填充概览数据
        writer.writeCellValue("B2", "时间：" + beginDate + " 至 " + endDate);
        writer.writeCellValue("C4", businessData.getTurnover());
        writer.writeCellValue("E4", businessData.getOrderCompletionRate());
        writer.writeCellValue("G4", businessData.getNewUsers());
        writer.writeCellValue("C5", businessData.getValidOrderCount());
        writer.writeCellValue("E5", businessData.getUnitPrice());

        // 4. 数据预处理：将字符串分割为数组
        List<LocalDate> dateList = getDateList(beginDate, endDate);
        String[] turnoverList = StringUtils.split(turnoverData.getTurnoverList(), ',');
        String[] validOrderList = StringUtils.split(orderData.getValidOrderCountList(), ',');
        String[] orderCountList = StringUtils.split(orderData.getOrderCountList(), ',');
        String[] newUserList = StringUtils.split(userData.getNewUserList(), ',');

        // 5. 在内存中计算并填充明细数据
        for (int i = 0; i < 30; i++) {
            LocalDate date = dateList.get(i);
            BigDecimal turnover = new BigDecimal(turnoverList[i]);
            int totalOrderCount = Integer.parseInt(orderCountList[i]);
            int validOrderCount = Integer.parseInt(validOrderList[i]);
            int newUsers = Integer.parseInt(newUserList[i]);

            // 在后端内存中计算每日订单完成率和平均客单价
            double completionRate = (totalOrderCount == 0) ? 0.0 : (double) validOrderCount / totalOrderCount;
            double unitPrice = (validOrderCount == 0) ? 0.0 : turnover.doubleValue() / validOrderCount;

            // Hutool的坐标也是从0开始的
            writer.writeCellValue(1, 7 + i, date.toString()); // B列，第8行开始
            writer.writeCellValue(2, 7 + i, turnover.doubleValue()); // C列
            writer.writeCellValue(3, 7 + i, validOrderCount); // D列
            writer.writeCellValue(4, 7 + i, completionRate); // E列
            writer.writeCellValue(5, 7 + i, unitPrice); // F列
            writer.writeCellValue(6, 7 + i, newUsers); // G列
        }

        // 6. 设置响应头并写出文件
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "运营数据报表-" + beginDate + "-" + endDate + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO-8859-1"));

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);

        // 7. 关闭资源
        out.close();
        writer.close();
    }

    /**
     * 使用 Apache POI 实现导出报表
     * @param response
     * @throws IOException
     */
    private void exportWithPOI(HttpServletResponse response) throws IOException {
        // 1. 获取近30天的业务数据
        LocalDate beginDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(beginDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        TurnoverReportVO turnoverData = turnoverStatistics(beginDate, endDate);
        OrderReportVO orderData = ordersStatistics(beginDate, endDate);
        UserReportVO userData = userStatistics(beginDate, endDate);

        // 2. 加载模板文件到内存
        // 从类路径下加载资源
        InputStream templateStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(templateStream);
        XSSFSheet sheet = workbook.getSheet("Sheet1");

        // 3. 填充概览数据
        sheet.getRow(1).getCell(1).setCellValue("时间：" + beginDate + " 至 " + endDate);
        sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
        sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
        sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
        sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
        sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

        // 4. 数据预处理
        List<LocalDate> dateList = getDateList(beginDate, endDate);
        String[] turnoverList = StringUtils.split(turnoverData.getTurnoverList(), ',');
        String[] validOrderList = StringUtils.split(orderData.getValidOrderCountList(), ',');
        String[] orderCountList = StringUtils.split(orderData.getOrderCountList(), ',');
        String[] newUserList = StringUtils.split(userData.getNewUserList(), ',');

        // 5. 在内存中计算并填充明细数据
        for (int i = 0; i < 30; i++) {
            LocalDate date = dateList.get(i);
            XSSFRow row = sheet.getRow(7 + i);

            BigDecimal turnover = new BigDecimal(turnoverList[i]);
            int totalOrderCount = Integer.parseInt(orderCountList[i]);
            int validOrderCount = Integer.parseInt(validOrderList[i]);
            int newUsers = Integer.parseInt(newUserList[i]);

            double completionRate = (totalOrderCount == 0) ? 0.0 : (double) validOrderCount / totalOrderCount;
            double unitPrice = (validOrderCount == 0) ? 0.0 : turnover.doubleValue() / validOrderCount;

            row.getCell(1).setCellValue(date.toString());
            row.getCell(2).setCellValue(turnover.doubleValue());
            row.getCell(3).setCellValue(validOrderCount);
            row.getCell(4).setCellValue(completionRate);
            row.getCell(5).setCellValue(unitPrice);
            row.getCell(6).setCellValue(newUsers);
        }

        // 6. 设置响应头并写出文件
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "运营数据报表-" + beginDate + "-" + endDate + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));

        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);

        // 7. 关闭资源
        out.close();
        workbook.close();
        templateStream.close();
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