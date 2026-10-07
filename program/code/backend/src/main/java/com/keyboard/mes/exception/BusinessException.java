package com.keyboard.mes.exception;

/**
 * 业务异常。
 *
 * <p>用于表达参数校验之外的业务失败，例如数据不存在、保存失败、删除失败等。</p>
 *
 * @author Keyboard MES项目组
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        this("BIZ_ERROR", message);
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
