package com.keyboard.mes.repository;

import com.keyboard.mes.entity.InspectionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 检验记录表数据访问接口。
 */
@Repository
@Mapper
public interface InspectionRecordMapper {

    ArrayList<InspectionRecord> inspectionRecordList();

    ArrayList<InspectionRecord> findByProductSn(@Param("productSn") String productSn);

    int insert(InspectionRecord inspectionRecord);

    InspectionRecord getById(@Param("id") Long id);

    int update(InspectionRecord inspectionRecord);

    int delete(@Param("id") Long id);
}
