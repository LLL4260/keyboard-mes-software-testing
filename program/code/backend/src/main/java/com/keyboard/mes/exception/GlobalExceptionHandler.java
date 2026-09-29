package com.keyboard.mes.exception;

import com.keyboard.mes.dto.Result;
import jakarta.validation.ConstraintViolationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <p>把业务异常和参数校验异常统一转换为 Result，避免接口返回结构不一致。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBusinessException(BusinessException exception) {
        log.warn("业务异常：{}", exception.getMessage());
        return Result.fail(400, exception.getMessage());
    }

    /**
     * 处理请求体参数校验异常。
     *
     * @param exception 请求体校验异常
     * @return 统一失败响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                // 一次返回全部字段错误，前端弹窗能直接提示用户需要修改哪些输入项。
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("；"));
        if (message.isBlank()) {
            message = "request body validation failed";
        }
        log.warn("请求体参数校验失败：{}", message);
        return Result.fail(400, message);
    }

    /**
     * 处理路径参数和查询参数校验异常。
     *
     * @param exception 参数校验异常
     * @return 统一失败响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleConstraintViolationException(ConstraintViolationException exception) {
        log.warn("请求参数校验失败：{}", exception.getMessage());
        return Result.fail(400, exception.getMessage());
    }

    /**
     * 处理未登录异常。
     *
     * @return 统一未登录响应
     */
    @ExceptionHandler(UnauthenticatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleUnauthenticatedException() {
        return Result.fail(401, "请先登录");
    }

    /**
     * 处理无权限异常。
     *
     * @return 统一无权限响应
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleUnauthorizedException() {
        return Result.fail(403, "无权限访问");
    }

    /**
     * 处理未预期异常，并记录完整堆栈。
     *
     * @param exception 未预期异常
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception exception) {
        log.error("系统未处理异常", exception);
        return Result.fail(500, "系统异常，请联系管理员");
    }
}
