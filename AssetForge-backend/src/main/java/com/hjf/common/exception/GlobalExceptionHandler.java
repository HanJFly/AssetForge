package com.hjf.common.exception;


import com.hjf.common.result.CommonException;
import com.hjf.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //捕获业务异常
    @ExceptionHandler(CommonException.class)
    public Result<String> handleCommonException(CommonException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    //捕获所有未处理的异常
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail("系统异常，请联系管理员");
    }

    //捕获参数校验异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<String> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.fail(400, msg);
    }
}
