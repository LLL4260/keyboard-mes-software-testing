package com.keyboard.mes.service;

import com.keyboard.mes.entity.InspectionRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 检验记录表业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface InspectionRecordService {

    /**
     * 查询检验记录列表。
     *
     * @return 检验记录列表
     */
    ArrayList<InspectionRecord> inspectionRecordList();

    /**
     * 查询检验记录可选业务数据。
     *
     * @return 业务数据
     */
    List<Map<String, Object>> inspectionOptions();

    /**
     * 保存检验记录。
     *
     * @param inspectionRecord 检验记录数据
     * @return 操作是否成功
     */
    boolean save(InspectionRecord inspectionRecord);

    /**
     * 按主键查询检验记录。
     *
     * @param id 记录主键
     * @return 查询或处理后的检验记录
     */
    InspectionRecord getInspectionRecordById(Long id);

    /**
     * 按主键删除检验记录。
     *
     * @param id 记录主键
     * @return 操作是否成功
     */
    boolean delete(Long id);

    /**
     * 更新检验记录。
     *
     * @param inspectionRecord 检验记录数据
     * @return 操作是否成功
     */
    boolean update(InspectionRecord inspectionRecord);

    /**
     * 提交检验记录并执行业务校验。
     *
     * @param inspectionRecord 检验记录数据
     * @return 查询或处理后的检验记录
     */
    InspectionRecord submit(InspectionRecord inspectionRecord);
}
