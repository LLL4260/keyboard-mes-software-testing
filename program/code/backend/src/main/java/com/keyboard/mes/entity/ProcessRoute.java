package com.keyboard.mes.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工艺路线表实体。
 *
 * <p>合并工序、路线步骤、工位和检验标准；用 inspection_config 保存检验项目配置。</p>
 *
 * @author Keyboard MES项目组
 */
@Data
public class ProcessRoute implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 产品型号ID */
    @NotNull(message = "产品型号不能为空")
    @Min(value = 1, message = "产品型号ID必须大于0")
    private Long productModelId;

    /** 工艺路线版本 */
    @NotBlank(message = "工艺路线版本不能为空")
    private String routeVersion;

    /** 工序顺序 */
    @NotNull(message = "工序顺序不能为空")
    @Min(value = 1, message = "工序顺序必须大于0")
    private Integer sequenceNo;

    /** 工序编码 */
    @NotBlank(message = "工序编码不能为空")
    private String processCode;

    /** 工序名称 */
    @NotBlank(message = "工序名称不能为空")
    private String processName;

    /** 工序类型：1=组装 2=检测 3=包装 4=返修 */
    @NotNull(message = "工序类型不能为空")
    @Min(value = 1, message = "工序类型范围为1到4")
    @Max(value = 4, message = "工序类型范围为1到4")
    private Integer processType;

    /** 默认工位编码 */
    private String stationCode;

    /** 默认工位名称 */
    private String stationName;

    /** 标准工时 */
    @DecimalMin(value = "0.00", message = "标准工时不能小于0")
    private BigDecimal standardHours;

    /** 是否质检节点：0=否 1=是 */
    @NotNull(message = "质检节点不能为空")
    @Min(value = 0, message = "质检节点只能为0或1")
    @Max(value = 1, message = "质检节点只能为0或1")
    private Integer qualityGate;

    /** 检验类型：process/final/AOI/ICT/FCT/recheck */
    private String inspectionType;

    /** 检验项目、标准值、公差、方法等JSON配置 */
    private String inspectionConfig;

    /** 作业指导书路径 */
    private String sopFile;

    /** 状态：0=停用 1=启用 */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;
}
