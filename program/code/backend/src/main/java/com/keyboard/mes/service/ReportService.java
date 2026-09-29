package com.keyboard.mes.service;

import java.util.Map;

/**
 * 轻量报表业务接口。
 */
public interface ReportService {

    Map<String, Object> overview();

    Map<String, Object> quality(String period, Long productModelId);

    Map<String, Object> orderTracking(Long orderId);
}
