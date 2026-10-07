package com.keyboard.mes.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 产品BOM表实体。
 *
 * <p>将 BOM 主表与明细表压平成一张 BOM 行表，保留版本、生效期和用量。</p>
 *
 * @author Keyboard MES项目组
 */
@Data
public class ProductBom implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 产品型号ID */
    @NotNull(message = "产品型号不能为空")
    @Min(value = 1, message = "产品型号ID必须大于0")
    private Long productModelId;

    /** BOM版本 */
    @NotBlank(message = "BOM版本不能为空")
    private String bomVersion;

    /** 物料ID */
    @NotNull(message = "物料不能为空")
    @Min(value = 1, message = "物料ID必须大于0")
    private Long materialId;

    /** 标准用量 */
    @NotNull(message = "标准用量不能为空")
    @DecimalMin(value = "0.001", message = "标准用量必须大于0")
    private BigDecimal quantity;

    /** 计量单位 */
    private String unit;

    /** BOM行序号 */
    @Min(value = 1, message = "BOM行序号必须大于0")
    private Integer sequenceNo;

    /** 生效日期 */
    private LocalDate effectiveDate;

    /** 失效日期 */
    private LocalDate expireDate;

    /** 状态：0=停用 1=启用 */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;

    /** 备注 */
    private String remark;
}
