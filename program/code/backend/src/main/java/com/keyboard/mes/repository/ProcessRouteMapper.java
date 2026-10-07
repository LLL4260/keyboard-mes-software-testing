package com.keyboard.mes.repository;

import com.keyboard.mes.entity.ProcessRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

/**
 * 工艺路线表数据访问接口。
 *
 * @author Keyboard MES项目组
 */
@Repository
@Mapper
public interface ProcessRouteMapper {

    /**
     * 查询工艺路线列表。
     *
     * @return 工艺路线列表
     */
    ArrayList<ProcessRoute> processRouteList();

    /**
     * 新增工艺路线。
     *
     * @param processRoute 工艺路线数据
     * @return 受影响的记录数
     */
    int insert(ProcessRoute processRoute);

    /**
     * 按主键查询工艺路线。
     *
     * @param id 记录主键
     * @return 查询或处理后的工艺路线
     */
    ProcessRoute getById(@Param("id") Long id);

    /**
     * 更新工艺路线。
     *
     * @param processRoute 工艺路线数据
     * @return 受影响的记录数
     */
    int update(ProcessRoute processRoute);

    /**
     * 按主键删除工艺路线。
     *
     * @param id 记录主键
     * @return 受影响的记录数
     */
    int delete(@Param("id") Long id);
}
