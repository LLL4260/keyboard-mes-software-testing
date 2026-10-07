package com.keyboard.mes.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产工单表实体。
 *
 * <p>合并生产订单、生产计划和派工单头信息，作为生产执行主线。</p>
 *
 * @author Keyboard MES项目组
 */
@Data
public class ProductionOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 生产工单编号 */
    @NotBlank(message = "生产工单编号不能为空")
    private String orderNo;

    /** 产品型号ID */
    @NotNull(message = "产品型号不能为空")
    @Min(value = 1, message = "产品型号ID必须大于0")
    private Long productModelId;

    /** 绑定BOM版本 */
    @NotBlank(message = "BOM版本不能为空")
    private String bomVersion;

    /** 绑定工艺路线版本 */
    @NotBlank(message = "工艺路线版本不能为空")
    private String routeVersion;

    /** 生产批次号 */
    private String batchNo;

    /** 工单数量 */
    @NotNull(message = "工单数量不能为空")
    @Min(value = 1, message = "工单数量必须大于0")
    private Integer quantity;

    /** 计划开始时间 */
    private LocalDateTime plannedStartTime;

    /** 计划结束时间 */
    private LocalDateTime plannedEndTime;

    /** 交付日期 */
    private LocalDate deliveryDate;

    /** 计划产线/班组 */
    private String lineName;

    /** 来源：ERP/manual */
    private String source;

    /** 优先级：0=普通 1=加急 */
    @NotNull(message = "优先级不能为空")
    @Min(value = 0, message = "优先级只能为0或1")
    @Max(value = 1, message = "优先级只能为0或1")
    private Integer priority;

    /** 计划/派工负责人ID */
    private Long plannerId;

    /** 物料齐套状态：0=未齐套 1=齐套 */
    @NotNull(message = "物料齐套状态不能为空")
    @Min(value = 0, message = "物料齐套状态只能为0或1")
    @Max(value = 1, message = "物料齐套状态只能为0或1")
    private Integer materialReadyStatus;

    /** 状态：0=待配置 1=已接受 2=生产中 3=待终检 4=完成 5=关闭 */
    @NotNull(message = "工单状态不能为空")
    @Min(value = 0, message = "工单状态范围为0到5")
    @Max(value = 5, message = "工单状态范围为0到5")
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
