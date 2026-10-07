package com.keyboard.mes.repository;

import com.keyboard.mes.entity.WorkReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 报工记录表数据访问接口。
 *
 * @author Keyboard MES项目组
 */
@Repository
@Mapper
public interface WorkReportMapper {

    /**
     * 查询报工记录列表。
     *
     * @return 报工记录列表
     */
    ArrayList<WorkReport> workReportList();

    /**
     * 按产品SN查询报工记录。
     *
     * @param productSn 产品SN
     * @return 报工记录列表
     */
    ArrayList<WorkReport> findByProductSn(@Param("productSn") String productSn);

    /**
     * 新增报工记录。
     *
     * @param workReport 报工记录数据
     * @return 受影响的记录数
     */
    int insert(WorkReport workReport);

    /**
     * 按主键查询报工记录。
     *
     * @param id 记录主键
     * @return 查询或处理后的报工记录
     */
    WorkReport getById(@Param("id") Long id);

    /**
     * 更新报工记录。
     *
     * @param workReport 报工记录数据
     * @return 受影响的记录数
     */
    int update(WorkReport workReport);

    /**
     * 按主键删除报工记录。
     *
     * @param id 记录主键
     * @return 受影响的记录数
     */
    int delete(@Param("id") Long id);
}
