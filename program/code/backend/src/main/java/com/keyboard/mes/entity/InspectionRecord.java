package com.keyboard.mes.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 检验记录表实体。
 *
 * <p>合并过程质检、成品终检、自动检测和不良处理，标准快照来自工艺路线配置。</p>
 *
 * @author Keyboard MES项目组
 */
@Data
public class InspectionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 检验单号 */
    @NotBlank(message = "检验单号不能为空")
    private String inspectionNo;

    /** 检验类型：process/final/recheck/AOI/ICT/FCT */
    @NotBlank(message = "检验类型不能为空")
    private String inspectionType;

    /** 生产任务ID */
    private Long taskId;

    /** 生产工单ID */
    @NotNull(message = "生产工单不能为空")
    @Min(value = 1, message = "生产工单ID必须大于0")
    private Long orderId;

    /** 产品条码/SN */
    private String productSn;

    /** 工艺路线步骤ID */
    private Long processRouteId;

    /** 检验标准快照JSON，避免标准变更影响历史记录 */
    private String standardSnapshot;

    /** 检验员ID；自动检测可为空 */
    private Long inspectorId;

    /** 数据来源：manual/equipment */
    @NotBlank(message = "数据来源不能为空")
    private String sourceType;

    /** 检测设备编号，AOI/ICT/FCT时填写 */
    private String equipmentNo;

    /** 检验时间 */
    @NotNull(message = "检验时间不能为空")
    private LocalDateTime inspectionTime;

    /** 结果：0=不合格 1=合格 2=让步接收 3=报废 */
    @NotNull(message = "检验结果不能为空")
    @Min(value = 0, message = "检验结果范围为0到3")
    @Max(value = 3, message = "检验结果范围为0到3")
    private Integer result;

    /** 实测数据或设备报告JSON */
    private String measuredData;

    /** 不合格原因 */
    private String defectReason;

    /** 处置方式：1=返修 2=报废 3=让步接收 */
    @Min(value = 1, message = "处置方式范围为1到3")
    @Max(value = 3, message = "处置方式范围为1到3")
    private Integer handlingMethod;

    /** 审批状态：0=无需/待审 1=通过 2=驳回 */
    @NotNull(message = "审批状态不能为空")
    @Min(value = 0, message = "审批状态范围为0到2")
    @Max(value = 2, message = "审批状态范围为0到2")
    private Integer approvalStatus;

    /** 备注 */
    private String remark;
}
