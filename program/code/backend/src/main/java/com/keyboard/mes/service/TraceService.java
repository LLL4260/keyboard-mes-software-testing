package com.keyboard.mes.service;

import com.keyboard.mes.dto.TraceResult;

/**
 * 产品追溯业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface TraceService {

    /**
     * 按产品SN查询追溯结果。
     *
     * @param productSn 产品SN
     * @return 查询或处理后的产品追溯
     */
    TraceResult getTraceByProductSn(String productSn);
}
