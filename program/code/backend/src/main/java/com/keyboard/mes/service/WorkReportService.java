package com.keyboard.mes.service;

import com.keyboard.mes.entity.WorkReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报工记录表业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface WorkReportService {

    /**
     * 查询报工记录列表。
     *
     * @return 报工记录列表
     */
    ArrayList<WorkReport> workReportList();

    /**
     * 查询报工记录可选业务数据。
     *
     * @return 业务数据
     */
    List<Map<String, Object>> reportOptions();

    /**
     * 保存报工记录。
     *
     * @param workReport 报工记录数据
     * @return 操作是否成功
     */
    boolean save(WorkReport workReport);

    /**
     * 按主键查询报工记录。
     *
     * @param id 记录主键
     * @return 查询或处理后的报工记录
     */
    WorkReport getWorkReportById(Long id);

    /**
     * 按主键删除报工记录。
     *
     * @param id 记录主键
     * @return 操作是否成功
     */
    boolean delete(Long id);

    /**
     * 更新报工记录。
     *
     * @param workReport 报工记录数据
     * @return 操作是否成功
     */
    boolean update(WorkReport workReport);

    /**
     * 提交报工记录并执行业务校验。
     *
     * @param workReport 报工记录数据
     * @return 查询或处理后的报工记录
     */
    WorkReport submit(WorkReport workReport);
}
