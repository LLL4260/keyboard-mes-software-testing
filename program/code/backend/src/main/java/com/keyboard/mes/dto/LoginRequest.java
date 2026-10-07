package com.keyboard.mes.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求参数。
 *
 * @author Keyboard MES项目组
 */
public class LoginRequest {

    /** 工号/登录账号 */
    @NotBlank(message = "工号不能为空")
    private String employeeNo;

    /** 登录密码明文，后端通过 Shiro 做 MD5 匹配 */
    @NotBlank(message = "密码不能为空")
    private String password;

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
