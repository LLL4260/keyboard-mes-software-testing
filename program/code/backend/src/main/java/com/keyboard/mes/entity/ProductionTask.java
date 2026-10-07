package com.keyboard.mes.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 生产任务表实体。
 *
 * <p>工序执行任务，同时承载轻量异常记录，避免再建独立异常工单表。</p>
 *
 * @author Keyboard MES项目组
 */
@Data
public class ProductionTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 任务单号 */
    @NotBlank(message = "任务单号不能为空")
    private String taskNo;

    /** 生产工单ID */
    @NotNull(message = "生产工单不能为空")
    @Min(value = 1, message = "生产工单ID必须大于0")
    private Long orderId;

    /** 工艺路线步骤ID */
    @NotNull(message = "工艺步骤不能为空")
    @Min(value = 1, message = "工艺步骤ID必须大于0")
    private Long processRouteId;

    /** 工序顺序 */
    @NotNull(message = "工序顺序不能为空")
    @Min(value = 1, message = "工序顺序必须大于0")
    private Integer sequenceNo;

    /** 实际执行工位 */
    private String stationCode;

    /** 指派操作员/班组长ID */
    private Long assignedUserId;

    /** 计划数量 */
    @NotNull(message = "计划数量不能为空")
    @Min(value = 1, message = "计划数量必须大于0")
    private Integer plannedQuantity;

    /** 已完工数量 */
    @NotNull(message = "完工数量不能为空")
    @Min(value = 0, message = "完工数量不能小于0")
    private Integer completedQuantity;

    /** 不良数量 */
    @NotNull(message = "不良数量不能为空")
    @Min(value = 0, message = "不良数量不能小于0")
    private Integer defectQuantity;

    /** 异常类型：1=缺料 2=设备 3=质量 4=其他 */
    @Min(value = 1, message = "异常类型范围为1到4")
    @Max(value = 4, message = "异常类型范围为1到4")
    private Integer abnormalType;

    /** 异常描述 */
    private String abnormalDesc;

    /** 异常处理措施 */
    private String abnormalSolution;

    /** 状态：0=待开工 1=生产中 2=暂停 3=已完工 4=待质检 5=异常 */
    @NotNull(message = "任务状态不能为空")
    @Min(value = 0, message = "任务状态范围为0到5")
    @Max(value = 5, message = "任务状态范围为0到5")
    private Integer status;

    /** 实际开始时间 */
    private LocalDateTime startTime;

    /** 实际完成时间 */
    private LocalDateTime finishTime;

    /** 备注 */
    private String remark;
}
