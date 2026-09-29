package com.keyboard.mes.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品型号表实体。
 *
 * <p>维护定制化键盘型号及关键配置，是 BOM、工艺路线和测试要求的主索引。</p>
 */
@Data
public class ProductModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 型号编码 */
    @NotBlank(message = "型号编码不能为空")
    private String modelCode;

    /** 型号名称 */
    @NotBlank(message = "型号名称不能为空")
    private String modelName;

    /** 产品类别 */
    private String category;

    /** 外壳、PCB、轴体、键帽、灯效、连接方式等配置摘要 */
    private String configurationDesc;

    /** 固件/功能版本 */
    private String firmwareVersion;

    /** 状态：0=停用 1=启用 */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;
}
