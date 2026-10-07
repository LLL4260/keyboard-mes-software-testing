package com.keyboard.mes.dto;

/**
 * 统一接口返回结果。
 *
 * <p>参考 code-files 项目写法，使用 code、msg、data 三个字段承载接口返回值。</p>
 *
 * @author Keyboard MES项目组
 */
public class Result {

    private Integer code;
    private String msg;
    private Object data;

    private Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static Result success(Object data) {
        return new Result(200, "成功", data);
    }

    public static Result success(String msg) {
        return new Result(200, msg, null);
    }

    public static Result success(String msg, Object data) {
        return new Result(200, msg, data);
    }

    public static Result fail(String msg) {
        return new Result(500, msg, null);
    }

    public static Result fail(Integer code, String msg) {
        return new Result(code, msg, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
