package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.10 14:44
 */

@RestController
@RequestMapping("/admin/report")
@Slf4j
@Tag(name = "数据统计相关接口", description = "数据报表统计相关接口")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @GetMapping("/turnoverStatistics")
    @Operation(summary = "营业额统计", description = "根据起始时间和结束时间查询营业额统计数据")
    public Result<TurnoverReportVO> turnoverStatistics(
            @RequestParam LocalDate begin,
            @RequestParam LocalDate end) {
        log.info("根据起始时间和结束时间查询营业额统计数据, begin: {}, end: {}", begin, end);
        TurnoverReportVO turnoverReportVO = reportService.turnoverStatistics(begin, end);
        return Result.success(turnoverReportVO);
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @GetMapping("/userStatistics")
    @Operation(summary = "用户统计", description = "根据起始时间和结束时间查询用户统计数据")
    public Result<UserReportVO> userStatistics(
            @RequestParam LocalDate begin,
            @RequestParam LocalDate end) {
        log.info("根据起始时间和结束时间查询用户统计数据, begin: {}, end: {}", begin, end);
        UserReportVO userReportVO = reportService.userStatistics(begin, end);
        return Result.success(userReportVO);
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @GetMapping("/ordersStatistics")
    @Operation(summary = "订单统计", description = "根据起始时间和结束时间查询订单数量统计数据")
    public Result<OrderReportVO> ordersStatistics(
            @RequestParam LocalDate begin,
            @RequestParam LocalDate end) {
        log.info("根据起始时间和结束时间查询订单数量统计数据, begin: {}, end: {}", begin, end);
        OrderReportVO orderReportVO = reportService.ordersStatistics(begin, end);
        return Result.success(orderReportVO);
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @GetMapping("/top10")
    @Operation(summary = "销售额前十统计", description = "根据起始时间和结束时间查询销售额前十统计数据")
    public Result<SalesTop10ReportVO> top10Statistics(
            @RequestParam LocalDate begin,
            @RequestParam LocalDate end) {
        log.info("根据起始时间和结束时间查询销售额前十统计数据, begin: {}, end: {}", begin, end);
        SalesTop10ReportVO salesTop10ReportVO = reportService.top10Statistics(begin, end);
        return Result.success(salesTop10ReportVO);
    }

    @GetMapping("/export")
    @Operation(summary = "导出数据报表", description = "导出数据报表")
    public void exportBusinessData(HttpServletResponse response){
        log.info("导出数据报表");
        reportService.exportBusinessData(response);
    }
}
