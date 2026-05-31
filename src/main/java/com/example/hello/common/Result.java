package com.example.hello.common;

import java.io.Serializable;

/**
 * 统一响应结构。
 * code=1 成功，code=0 失败（见接口文档 0.1 统一响应格式）。
 *
 * @param <T> data 的类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 响应码：1 成功，0 失败 */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 返回的数据 */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(1, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(1, "success", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(1, msg, data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(0, msg, null);
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
