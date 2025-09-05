package xyz.zhenliang.rabbitmq.demo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz.zhenliang.rabbitmq.demo.dto.Result;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception ex) {

        int errorCode = ErrorCode.INTERNAL_ERROR.getCode(); // 默认错误码
        if (ex instanceof BizException) {
            // 如果是业务异常
            errorCode = ErrorCode.BIZ_ERROR.getCode();
        }
        if (errorCode != ErrorCode.BIZ_ERROR.getCode()) {
            // 记录日志，对于业务异常不记录日志
            log.error(ex.getMessage(), ex);
        }

        Result<?> result = Result.error(errorCode, ex.getMessage());
        return ResponseEntity
                .status(errorCode)
                .body(result);
    }
}
