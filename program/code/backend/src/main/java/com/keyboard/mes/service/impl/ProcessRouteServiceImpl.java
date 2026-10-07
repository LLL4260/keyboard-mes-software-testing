package com.keyboard.mes.service.impl;

import com.keyboard.mes.entity.ProcessRoute;
import com.keyboard.mes.repository.ProcessRouteMapper;
import com.keyboard.mes.service.ProcessRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 工艺路线表业务实现。
 *
 * @author Keyboard MES项目组
 */
@Service
public class ProcessRouteServiceImpl implements ProcessRouteService {

    @Autowired
    private ProcessRouteMapper processRouteMapper;

    @Override
    public ArrayList<ProcessRoute> processRouteList() {
        return processRouteMapper.processRouteList();
    }

    @Override
    public boolean save(ProcessRoute processRoute) {
        int num = processRouteMapper.insert(processRoute);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public ProcessRoute getProcessRouteById(Long id) {
        return processRouteMapper.getById(id);
    }

    @Override
    public boolean delete(Long id) {
        int num = processRouteMapper.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean update(ProcessRoute processRoute) {
        int num = processRouteMapper.update(processRoute);
        if (num > 0) {
            return true;
        }
        return false;
    }
}
