package xyz.zhenliang.rabbitmq.demo.exception;

/**
 * 业务异常类，用于抛出业务逻辑相关的异常
 * 继承自 RuntimeException，表示这是一个运行时异常
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
