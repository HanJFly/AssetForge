package com.hjf.common.result;

public class CommonException extends RuntimeException{

    private Integer code;

    public CommonException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }



}
