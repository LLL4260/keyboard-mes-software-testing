package com.keyboard.mes.service;

import com.keyboard.mes.dto.TraceResult;

/**
 * 产品追溯业务接口。
 */
public interface TraceService {

    TraceResult getTraceByProductSn(String productSn);
}
