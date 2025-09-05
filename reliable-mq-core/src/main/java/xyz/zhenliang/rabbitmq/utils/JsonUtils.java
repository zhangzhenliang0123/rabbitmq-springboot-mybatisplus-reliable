package xyz.zhenliang.rabbitmq.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.zhenliang.rabbitmq.exception.TechException;

import java.text.SimpleDateFormat;

public class JsonUtils {
    // 优化：使用final确保线程安全
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 新增：静态初始化配置
    static {
        objectMapper.findAndRegisterModules();
        // 配置日期格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 空对象不抛异常
        // objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new TechException(e);
        }

    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new TechException("JSON 转换失败: " + e.getOriginalMessage(), e);
        }
    }

    /**
     * 新增：支持泛型转换的方法
     *
     * @param json          JSON字符串
     * @param typeReference 类型引用(如 new TypeReference<List<User>>(){})
     * @param <T>           目标类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new TechException("JSON泛型转换失败: " + e.getOriginalMessage(), e);
        }
    }
}
