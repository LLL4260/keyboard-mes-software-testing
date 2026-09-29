package com.keyboard.mes.service;

import com.keyboard.mes.entity.InspectionRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 检验记录表业务接口。
 */
public interface InspectionRecordService {

    ArrayList<InspectionRecord> inspectionRecordList();

    List<Map<String, Object>> inspectionOptions();

    boolean save(InspectionRecord inspectionRecord);

    InspectionRecord getInspectionRecordById(Long id);

    boolean delete(Long id);

    boolean update(InspectionRecord inspectionRecord);

    InspectionRecord submit(InspectionRecord inspectionRecord);
}
