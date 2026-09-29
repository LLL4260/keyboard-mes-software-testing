package com.keyboard.mes.service;

import com.keyboard.mes.entity.WorkReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报工记录表业务接口。
 */
public interface WorkReportService {

    ArrayList<WorkReport> workReportList();

    List<Map<String, Object>> reportOptions();

    boolean save(WorkReport workReport);

    WorkReport getWorkReportById(Long id);

    boolean delete(Long id);

    boolean update(WorkReport workReport);

    WorkReport submit(WorkReport workReport);
}
