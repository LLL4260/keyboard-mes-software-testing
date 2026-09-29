package com.keyboard.mes.service;

import com.keyboard.mes.entity.ReworkOrder;
import com.keyboard.mes.entity.InspectionRecord;

import java.util.ArrayList;

/**
 * 返修工单表业务接口。
 */
public interface ReworkOrderService {

    ArrayList<ReworkOrder> reworkOrderList();

    boolean save(ReworkOrder reworkOrder);

    ReworkOrder getReworkOrderById(Long id);

    boolean delete(Long id);

    boolean update(ReworkOrder reworkOrder);

    ReworkOrder repair(Long id, ReworkOrder reworkOrder);

    ReworkOrder recheck(Long id, InspectionRecord inspectionRecord);
}
