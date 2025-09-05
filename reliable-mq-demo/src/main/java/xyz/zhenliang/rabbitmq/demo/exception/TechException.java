package xyz.zhenliang.rabbitmq.demo.exception;

/**
 * 技术异常
 */
public class TechException extends RuntimeException {
    public TechException(String message) {
        super(message);
    }
    public TechException(Throwable cause) {
            super(cause);
    }
    public TechException(String message, Throwable cause) {
        super(message, cause);
    }
}
