package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ProcessRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 工艺路线表数据访问接口。
 */
@Repository
@Mapper
public interface ProcessRouteMapper {

    ArrayList<ProcessRoute> processRouteList();

    int insert(ProcessRoute processRoute);

    ProcessRoute getById(@Param("id") Long id);

    int update(ProcessRoute processRoute);

    int delete(@Param("id") Long id);
}
