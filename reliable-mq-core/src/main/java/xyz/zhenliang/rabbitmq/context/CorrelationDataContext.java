package xyz.zhenliang.rabbitmq.context;

import org.springframework.amqp.rabbit.connection.CorrelationData;

public class CorrelationDataContext {
    private static final ThreadLocal<CorrelationData> CORRELATION_DATA = new ThreadLocal<>();

    public static void set(CorrelationData correlationData) {
        CORRELATION_DATA.set(correlationData);
    }

    public static CorrelationData get() {
        return CORRELATION_DATA.get();
    }

    public static void clear() {
        CORRELATION_DATA.remove();
    }
}
