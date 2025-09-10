package com.sky.service;

import com.sky.vo.TurnoverReportVO;

import java.time.LocalDate;

/**
 *
 * @author FragrantXue
 * Create by 2025.09.10 14:45
 */

public interface ReportService {
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);
}
