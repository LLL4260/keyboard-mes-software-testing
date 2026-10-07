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
 * 报工记录表实体。
 *
 * <p>记录操作员按任务提交的完工数量、产品条码、工时和不良信息。</p>
 *
 * @author Keyboard MES项目组
 */
@Data
public class WorkReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 报工单号 */
    @NotBlank(message = "报工单号不能为空")
    private String reportNo;

    /** 生产任务ID */
    @NotNull(message = "生产任务不能为空")
    @Min(value = 1, message = "生产任务ID必须大于0")
    private Long taskId;

    /** 生产工单ID */
    @NotNull(message = "生产工单不能为空")
    @Min(value = 1, message = "生产工单ID必须大于0")
    private Long orderId;

    /** 产品条码/SN；批量报工可为空 */
    private String productSn;

    /** 操作员ID */
    @NotNull(message = "操作员不能为空")
    @Min(value = 1, message = "操作员ID必须大于0")
    private Long operatorId;

    /** 报工工位 */
    private String stationCode;

    /** 报工数量 */
    @NotNull(message = "报工数量不能为空")
    @Min(value = 1, message = "报工数量必须大于0")
    private Integer reportQuantity;

    /** 合格数量 */
    @NotNull(message = "合格数量不能为空")
    @Min(value = 0, message = "合格数量不能小于0")
    private Integer qualifiedQuantity;

    /** 不良数量 */
    @NotNull(message = "不良数量不能为空")
    @Min(value = 0, message = "不良数量不能小于0")
    private Integer defectQuantity;

    /** 实际工时 */
    @DecimalMin(value = "0.00", message = "实际工时不能小于0")
    private BigDecimal actualHours;

    /** 不良原因 */
    private String defectReason;

    /** 报工时间 */
    @NotNull(message = "报工时间不能为空")
    private LocalDateTime reportTime;

    /** 状态：0=撤销 1=有效 2=待审核 */
    @NotNull(message = "报工状态不能为空")
    @Min(value = 0, message = "报工状态范围为0到2")
    @Max(value = 2, message = "报工状态范围为0到2")
    private Integer status;
}
