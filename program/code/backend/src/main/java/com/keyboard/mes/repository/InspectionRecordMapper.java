package com.keyboard.mes.repository;

import com.keyboard.mes.entity.InspectionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 检验记录表数据访问接口。
 *
 * @author Keyboard MES项目组
 */
@Repository
@Mapper
public interface InspectionRecordMapper {

    /**
     * 查询检验记录列表。
     *
     * @return 检验记录列表
     */
    ArrayList<InspectionRecord> inspectionRecordList();

    /**
     * 按产品SN查询检验记录。
     *
     * @param productSn 产品SN
     * @return 检验记录列表
     */
    ArrayList<InspectionRecord> findByProductSn(@Param("productSn") String productSn);

    /**
     * 新增检验记录。
     *
     * @param inspectionRecord 检验记录数据
     * @return 受影响的记录数
     */
    int insert(InspectionRecord inspectionRecord);

    /**
     * 按主键查询检验记录。
     *
     * @param id 记录主键
     * @return 查询或处理后的检验记录
     */
    InspectionRecord getById(@Param("id") Long id);

    /**
     * 更新检验记录。
     *
     * @param inspectionRecord 检验记录数据
     * @return 受影响的记录数
     */
    int update(InspectionRecord inspectionRecord);

    /**
     * 按主键删除检验记录。
     *
     * @param id 记录主键
     * @return 受影响的记录数
     */
    int delete(@Param("id") Long id);
}
