package xyz.zhenliang.rabbitmq.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.amqp.core.Message;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class RabbitmqUtils {

    /**
     * 从RabbitMQ消息中提取数据并转换为指定类型
     *
     * @param message RabbitMQ消息对象
     * @param clazz   目标类型Class
     * @param <T>     泛型类型
     * @return 转换后的对象，如果转换失败或消息为空则返回null
     */
    public static <T> T getMessageData(Message message, Class<T> clazz) {
        // 检查输入参数
        if (Objects.isNull(message) || Objects.isNull(clazz)) {
            return null;
        }

        // 获取消息体字节数组
        byte[] body = message.getBody();

        // 检查消息体是否为空
        if (Objects.isNull(body) || body.length == 0) {
            return null;
        }

        // 将字节数组转换为字符串（假设消息是文本格式）
        String content = new String(body, StandardCharsets.UTF_8);

        // 使用JsonUtils将字符串转换为指定类型的对象
        return JsonUtils.fromJson(content, clazz);
    }

    /**
     * 从RabbitMQ消息中提取数据并转换为指定类型
     *
     * @param message       RabbitMQ消息对象
     * @param typeReference 目标类型Class
     * @param <T>           泛型类型
     * @return 转换后的对象，如果转换失败或消息为空则返回null
     */
    public static <T> T getMessageData(Message message, TypeReference<T> typeReference) {
        // 检查输入参数
        if (Objects.isNull(message) || Objects.isNull(typeReference)) {
            return null;
        }

        // 获取消息体字节数组
        byte[] body = message.getBody();

        // 检查消息体是否为空
        if (Objects.isNull(body) || body.length == 0) {
            return null;
        }

        // 将字节数组转换为字符串（假设消息是文本格式）
        String content = new String(body, StandardCharsets.UTF_8);

        // 使用JsonUtils将字符串转换为指定类型的对象
        return JsonUtils.fromJson(content, typeReference);
    }

    /**
     * 将字节数组转为字符串
     *
     * @param array
     * @return
     */
    public static String toString(byte[] array) {
        return new String(array, StandardCharsets.UTF_8);
    }

    /**
     * 将对象转换为字节数组
     *
     * @param obj 需要转换的对象
     * @param <T> 对象类型
     * @return 字节数组，如果对象为空则返回null
     */
    public static <T> byte[] toByteArray(T obj) {
        // 检查输入参数
        if (Objects.isNull(obj)) {
            return null;
        }
        if (obj instanceof String) {
            return ((String) obj).getBytes(StandardCharsets.UTF_8);
        }

        // 将对象转换为JSON字符串
        String json = JsonUtils.toJson(obj);

        // 检查转换结果
        if (Objects.isNull(json)) {
            return null;
        }

        // 将JSON字符串转换为字节数组
        return json.getBytes(StandardCharsets.UTF_8);
    }
}