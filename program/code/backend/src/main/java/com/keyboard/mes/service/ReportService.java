package com.keyboard.mes.service;

import java.util.Map;

/**
 * 轻量报表业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface ReportService {

    /**
     * 查询生产概览。
     *
     * @return 业务数据
     */
    Map<String, Object> overview();

    /**
     * 按周期和产品型号查询质量分析。
     *
     * @param period 统计周期，支持day、week和month
     * @param productModelId 产品型号主键，为null时汇总全部型号
     * @return 业务数据
     */
    Map<String, Object> quality(String period, Long productModelId);

    /**
     * 查询工单执行进度。
     *
     * @param orderId 生产工单主键
     * @return 业务数据
     */
    Map<String, Object> orderTracking(Long orderId);
}
