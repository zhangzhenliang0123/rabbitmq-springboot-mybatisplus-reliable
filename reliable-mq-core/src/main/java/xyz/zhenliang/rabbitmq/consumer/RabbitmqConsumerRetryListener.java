package xyz.zhenliang.rabbitmq.consumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitmqConsumerRetryListener implements RetryListener {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqConsumerRetryListener.class);


    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        RetryListener.super.close(context, callback, throwable);
    }
}
