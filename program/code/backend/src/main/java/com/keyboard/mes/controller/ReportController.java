package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 轻量报表接口。
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** 查询小程序和管理台首页概览数据。 */
    @GetMapping("/overview")
    public Result overview() {
        log.info("查询生产概览报表");
        return Result.success(reportService.overview());
    }

    /** 查询质量和产量趋势轻量分析数据。 */
    @GetMapping("/quality")
    public Result quality(@RequestParam(name = "period", defaultValue = "week") String period,
                          @RequestParam(name = "productModelId", required = false) Long productModelId) {
        log.info("查询质量分析报表：period={}, productModelId={}", period, productModelId);
        return Result.success(reportService.quality(period, productModelId));
    }

    /** 查询指定生产工单的进度、质量和待办提醒。 */
    @GetMapping("/order/{id}")
    public Result orderTracking(@PathVariable("id") Long id) {
        log.info("查询工单跟踪报表：orderId={}", id);
        return Result.success(reportService.orderTracking(id));
    }
}
