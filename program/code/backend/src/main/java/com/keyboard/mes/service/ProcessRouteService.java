package com.keyboard.mes.service;

import com.keyboard.mes.entity.ProcessRoute;

import java.util.ArrayList;

/**
 * 工艺路线表业务接口。
 */
public interface ProcessRouteService {

    ArrayList<ProcessRoute> processRouteList();

    boolean save(ProcessRoute processRoute);

    ProcessRoute getProcessRouteById(Long id);

    boolean delete(Long id);

    boolean update(ProcessRoute processRoute);
}
