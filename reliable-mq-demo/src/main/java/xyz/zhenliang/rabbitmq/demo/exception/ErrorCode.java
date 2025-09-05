package xyz.zhenliang.rabbitmq.demo.exception;

public enum ErrorCode {
    SUCCESS(200, "成功"),
    BIZ_ERROR(422, "业务错误"),
    UNAUTHORIZED(401, "未授权"),
    INTERNAL_ERROR(500, "系统内部错误");


    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
