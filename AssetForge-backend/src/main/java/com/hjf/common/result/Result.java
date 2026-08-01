package com.hjf.common.result;


import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    //私有构造，外部不能new
    private Result() {}

/**
 * 成功响应方法，无数据返回
 * @param <T> 泛型类型，表示返回数据类型
 * @return Result<T> 返回一个成功状态的结果对象
 */
    //成功，无数据
    public static <T> Result<T> ok() {
    // 创建一个Result对象实例
        Result<T> r = new Result<>();
    // 设置响应码为200，表示成功
        r.setCode(200);
    // 设置响应消息为"success"
        r.setMsg("success");
    // 返回设置好的Result对象
        return r;
    }

    //成功，有数据
    public static <T> Result<T> ok(T data){
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }
    
    //失败
    public static <T> Result<T> fail(String msg){
        Result<T> r = new Result<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }

    //失败，带自定义码
    public static <T> Result<T> fail(Integer code, String msg){
        Result<T> r = new Result<>();
        r.setCode( code);
        r.setMsg(msg);
        return r;
    }

}
