package xyz.zhenliang.rabbitmq.demo.dto;


import xyz.zhenliang.rabbitmq.demo.exception.ErrorCode;

/**
 * 统一返回结果封装类
 * @param <T> 返回数据类型
 */
public class Result<T> {
    /**
     * 请求是否成功
     */
    private boolean success;
    
    /**
     * 状态码
     */
    private int code;
    
    /**
     * 返回信息
     */
    private String message;
    
    /**
     * 返回数据
     */
    private T data;

    /**
     * 无参构造函数
     */
    public Result() {
    }

    /**
     * 全参构造函数
     * @param success 是否成功
     * @param code 状态码
     * @param message 返回信息
     * @param data 返回数据
     */
    public Result(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 请求成功，无返回数据
     * @param <T> 返回数据类型
     * @return Result对象
     */
    public static <T> Result<T> ok() {
        return new Result<>(true, ErrorCode.SUCCESS.getCode(), "", null);
    }
    
    /**
     * 请求成功，返回数据
     * @param data 返回数据
     * @param <T> 返回数据类型
     * @return Result对象
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(true, ErrorCode.SUCCESS.getCode(), "", data);
    }

    /**
     * 请求成功，返回信息和数据
     * @param message 返回信息
     * @param data 返回数据
     * @param <T> 返回数据类型
     * @return Result对象
     */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(true, ErrorCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 请求失败，返回错误信息
     * @param message 错误信息
     * @param <T> 返回数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(false, ErrorCode.INTERNAL_ERROR.getCode(), message, null);
    }

    /**
     * 请求失败，返回错误码和错误信息
     * @param code 错误码
     * @param message 错误信息
     * @param <T> 返回数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(false, code, message, null);
    }

    /**
     * 请求失败，返回错误码、错误信息和数据
     * @param code 错误码
     * @param message 错误信息
     * @param data 返回数据
     * @param <T> 返回数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(false, code, message, data);
    }

    // getter/setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}