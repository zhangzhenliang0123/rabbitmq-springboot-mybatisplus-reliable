package xyz.zhenliang.rabbitmq.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import xyz.zhenliang.rabbitmq.entity.RabbitmqMessage;
import xyz.zhenliang.rabbitmq.reminder.IRabbitmqReminder;
import xyz.zhenliang.rabbitmq.service.IRabbitmqMessageService;
import xyz.zhenliang.rabbitmq.utils.MqSpringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class RabbitmqReminderService {
    private static final Logger log = LoggerFactory.getLogger(RabbitmqReminderService.class);

    @Autowired
    private IRabbitmqMessageService rabbitmqMessageService;

    /**
     * 发送失败提醒方法
     * 当消息发送失败时调用此方法进行提醒
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendFailedReminder(String messageId, Throwable e) {
        RabbitmqMessage message = rabbitmqMessageService.getById(messageId);
        if (message != null) {
            this.sendFailedReminder(messageId, message.getMessageBody(), e);
        }
    }

    /**
     * 发送失败提醒方法
     * 当消息发送失败时调用此方法进行提醒
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendFailedReminder(String messageId, String cause) {
        RabbitmqMessage message = rabbitmqMessageService.getById(messageId);
        if (message != null) {
            IRabbitmqReminder rabbitmqReminder = MqSpringUtils.getBean(IRabbitmqReminder.class);
            if (rabbitmqReminder == null) return;
            rabbitmqReminder.sendFailedReminder(messageId, message.getMessageBody(), cause);
        }
    }

    /**
     * 发送失败提醒方法
     * 当消息发送失败时调用此方法进行提醒
     */
    public void sendFailedReminder(String messageId, String messageBody, Throwable e) {
        try {
            IRabbitmqReminder rabbitmqReminder = MqSpringUtils.getBean(IRabbitmqReminder.class);
            if (rabbitmqReminder == null) return;
            // 修改:将e.getMessage()改为获取异常堆栈信息
            String errorMessage = getStackTraceAsString(e);
            rabbitmqReminder.sendFailedReminder(messageId, messageBody, errorMessage);
        } catch (Exception ex) {
            log.error(ex.getMessage(), e);
        }

    }

    /**
     * 消费失败提醒方法
     * 当消息消费失败时调用此方法进行提醒
     */
    public void consumeFailedReminder(String messageId, String messageBody, Throwable e) {
        try {
            IRabbitmqReminder rabbitmqReminder = MqSpringUtils.getBean(IRabbitmqReminder.class);
            if (rabbitmqReminder == null) return;
            // 修改:将e.getMessage()改为获取异常堆栈信息
            String errorMessage = getStackTraceAsString(e);
            rabbitmqReminder.consumeFailedReminder(messageId, messageBody, errorMessage);
        } catch (Exception ex) {
            log.error(ex.getMessage(), e);
        }
    }

    /**
     * 将异常堆栈信息转为字符串
     *
     * @param e 异常对象
     * @return 异常堆栈信息字符串
     */
    public String getStackTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}