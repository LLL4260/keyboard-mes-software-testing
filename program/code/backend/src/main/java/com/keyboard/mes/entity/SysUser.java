package com.keyboard.mes.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户表实体。
 *
 * <p>合并员工、角色、权限关联，仅保留登录账号、岗位角色和操作范围。</p>
 *
 * @author Keyboard MES项目组
 */
@Data
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 工号/登录账号 */
    @NotBlank(message = "工号不能为空")
    private String employeeNo;

    /** 登录密码密文 */
    @NotBlank(message = "密码不能为空")
    private String passwordHash;

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /** 角色编码：admin/planner/operator/inspector/repair */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /** 角色名称 */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /** 部门/班组 */
    private String department;

    /** 岗位 */
    private String position;

    /** 手机号 */
    private String phone;

    /** 技能等级 */
    private String skillLevel;

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
