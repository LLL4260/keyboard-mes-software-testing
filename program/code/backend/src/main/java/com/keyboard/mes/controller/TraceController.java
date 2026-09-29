package com.keyboard.mes.controller;

import com.keyboard.mes.dto.Result;
import com.keyboard.mes.service.TraceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品追溯接口。
 */
@RestController
@RequestMapping("/api/trace")
public class TraceController {

    private static final Logger log = LoggerFactory.getLogger(TraceController.class);

    private final TraceService traceService;

    public TraceController(TraceService traceService) {
        this.traceService = traceService;
    }

    @GetMapping("/{productSn}")
    public Result trace(@PathVariable("productSn") String productSn) {
        log.info("查询产品追溯：productSn={}", productSn);
        return Result.success(traceService.getTraceByProductSn(productSn));
    }
}
