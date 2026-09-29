package com.keyboard.mes.dto;

import com.keyboard.mes.entity.InspectionRecord;
import com.keyboard.mes.entity.ProductionOrder;
import com.keyboard.mes.entity.ProductionTask;
import com.keyboard.mes.entity.ReworkOrder;
import com.keyboard.mes.entity.WorkReport;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 产品追溯结果。
 */
@Data
public class TraceResult {

    private String productSn;

    private List<ProductionOrder> orders = new ArrayList<>();

    private List<ProductionTask> tasks = new ArrayList<>();

    private List<WorkReport> reports = new ArrayList<>();

    private List<InspectionRecord> inspections = new ArrayList<>();

    private List<ReworkOrder> reworks = new ArrayList<>();
}
