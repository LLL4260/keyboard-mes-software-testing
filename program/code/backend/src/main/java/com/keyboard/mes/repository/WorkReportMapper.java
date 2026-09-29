package com.keyboard.mes.repository;

import com.keyboard.mes.entity.WorkReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 报工记录表数据访问接口。
 */
@Repository
@Mapper
public interface WorkReportMapper {

    ArrayList<WorkReport> workReportList();

    ArrayList<WorkReport> findByProductSn(@Param("productSn") String productSn);

    int insert(WorkReport workReport);

    WorkReport getById(@Param("id") Long id);

    int update(WorkReport workReport);

    int delete(@Param("id") Long id);
}
