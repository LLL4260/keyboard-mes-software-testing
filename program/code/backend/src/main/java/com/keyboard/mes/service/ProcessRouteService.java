package com.keyboard.mes.service;

import com.keyboard.mes.entity.ProcessRoute;

import java.util.ArrayList;

/**
 * 工艺路线表业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface ProcessRouteService {

    /**
     * 查询工艺路线列表。
     *
     * @return 工艺路线列表
     */
    ArrayList<ProcessRoute> processRouteList();

    /**
     * 保存工艺路线。
     *
     * @param processRoute 工艺路线数据
     * @return 操作是否成功
     */
    boolean save(ProcessRoute processRoute);

    /**
     * 按主键查询工艺路线。
     *
     * @param id 记录主键
     * @return 查询或处理后的工艺路线
     */
    ProcessRoute getProcessRouteById(Long id);

    /**
     * 按主键删除工艺路线。
     *
     * @param id 记录主键
     * @return 操作是否成功
     */
    boolean delete(Long id);

    /**
     * 更新工艺路线。
     *
     * @param processRoute 工艺路线数据
     * @return 操作是否成功
     */
    boolean update(ProcessRoute processRoute);
}
