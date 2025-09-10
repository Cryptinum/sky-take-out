package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}
