package com.keyboard.mes.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 返修工单表实体。
 *
 * <p>记录不合格产品从返修发起、分发、维修、复检到关闭的闭环过程。</p>
 */
@Data
public class ReworkOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 返修工单号 */
    @NotBlank(message = "返修工单号不能为空")
    private String reworkNo;

    /** 来源检验记录ID */
    @NotNull(message = "来源检验不能为空")
    @Min(value = 1, message = "来源检验ID必须大于0")
    private Long sourceInspectionId;

    /** 原生产工单ID */
    @NotNull(message = "生产工单不能为空")
    @Min(value = 1, message = "生产工单ID必须大于0")
    private Long orderId;

    /** 来源任务ID */
    private Long taskId;

    /** 产品条码/SN */
    private String productSn;

    /** 不合格原因 */
    private String defectReason;

    /** 返修要求 */
    private String reworkRequirement;

    /** 维修责任人ID */
    private Long assigneeId;

    /** 返修截止时间 */
    private LocalDateTime deadline;

    /** 状态：0=待分发 1=返修中 2=待复检 3=完成 4=关闭 */
    @NotNull(message = "返修状态不能为空")
    @Min(value = 0, message = "返修状态范围为0到4")
    @Max(value = 4, message = "返修状态范围为0到4")
    private Integer status;

    /** 维修措施 */
    private String repairAction;

    /** 更换物料摘要 */
    private String replacedMaterial;

    /** 维修工时 */
    @DecimalMin(value = "0.00", message = "维修工时不能小于0")
    private BigDecimal repairHours;

    /** 维修结果 */
    private String repairResult;

    /** 维修完成时间 */
    private LocalDateTime repairTime;

    /** 复检记录ID */
    private Long recheckInspectionId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
