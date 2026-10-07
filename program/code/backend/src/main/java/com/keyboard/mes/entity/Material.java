package com.keyboard.mes.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物料表实体。
 *
 * <p>维护键盘零部件、辅料、半成品和成品物料档案。</p>
 *
 * @author Keyboard MES项目组
 */
@Data
public class Material implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 物料编码 */
    @NotBlank(message = "物料编码不能为空")
    private String materialCode;

    /** 物料名称 */
    @NotBlank(message = "物料名称不能为空")
    private String materialName;

    /** 规格型号 */
    private String specification;

    /** 物料类型：1=零部件 2=辅料 3=半成品 4=成品 */
    @NotNull(message = "物料类型不能为空")
    @Min(value = 1, message = "物料类型范围为1到4")
    @Max(value = 4, message = "物料类型范围为1到4")
    private Integer materialType;

    /** 计量单位 */
    private String unit;

    /** 供应商/来源 */
    private String supplier;

    /** 物料条码或批次条码 */
    private String barcode;

    /** 状态：0=停用 1=启用 */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
