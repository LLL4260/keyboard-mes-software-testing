package com.keyboard.mes.service;

import com.keyboard.mes.entity.ReworkOrder;
import com.keyboard.mes.entity.InspectionRecord;

import java.util.ArrayList;

/**
 * 返修工单表业务接口。
 *
 * @author Keyboard MES项目组
 */
public interface ReworkOrderService {

    /**
     * 查询返修工单列表。
     *
     * @return 返修工单列表
     */
    ArrayList<ReworkOrder> reworkOrderList();

    /**
     * 保存返修工单。
     *
     * @param reworkOrder 返修工单数据
     * @return 操作是否成功
     */
    boolean save(ReworkOrder reworkOrder);

    /**
     * 按主键查询返修工单。
     *
     * @param id 记录主键
     * @return 查询或处理后的返修工单
     */
    ReworkOrder getReworkOrderById(Long id);

    /**
     * 按主键删除返修工单。
     *
     * @param id 记录主键
     * @return 操作是否成功
     */
    boolean delete(Long id);

    /**
     * 更新返修工单。
     *
     * @param reworkOrder 返修工单数据
     * @return 操作是否成功
     */
    boolean update(ReworkOrder reworkOrder);

    /**
     * 登记返修结果。
     *
     * @param id 记录主键
     * @param reworkOrder 返修工单数据
     * @return 查询或处理后的返修工单
     */
    ReworkOrder repair(Long id, ReworkOrder reworkOrder);

    /**
     * 提交返修复检结果。
     *
     * @param id 记录主键
     * @param inspectionRecord 返修工单数据
     * @return 查询或处理后的返修工单
     */
    ReworkOrder recheck(Long id, InspectionRecord inspectionRecord);
}
