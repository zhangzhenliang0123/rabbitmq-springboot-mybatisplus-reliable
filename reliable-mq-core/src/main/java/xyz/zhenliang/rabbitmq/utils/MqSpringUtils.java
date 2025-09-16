package xyz.zhenliang.rabbitmq.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Spring工具类，用于获取Spring容器中的Bean实例和环境信息
 * 实现ApplicationContextAware接口，Spring启动时会自动注入ApplicationContext
 *
 * @author zzl
 * @version 1.0
 */
@Component("rabbitMqSpringUtils")
public class MqSpringUtils implements ApplicationContextAware {
    /**
     * Spring应用上下文，用于获取容器中的Bean
     */
    private static ApplicationContext applicationContext;

    /**
     * 设置应用上下文，由Spring容器自动调用
     *
     * @param applicationContext Spring应用上下文
     * @throws BeansException Bean异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MqSpringUtils.applicationContext = applicationContext;
    }

    /**
     * 获取Spring应用上下文
     *
     * @return ApplicationContext 应用上下文实例
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 根据类型从Spring容器中获取Bean实例
     *
     * @param clazz Bean的Class类型
     * @param <T>   Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            return null;
        }
    }

    /**
     * 根据名称从Spring容器中获取Bean实例
     *
     * @param name Bean的名称
     * @return Bean实例
     */
    public static Object getBean(String name) {
        try {
            return applicationContext.getBean(name);
        } catch (BeansException e) {
            return null;
        }
    }

    /**
     * 获取当前激活的环境配置名称
     *
     * @return 当前激活的环境配置名称，如dev、test、prod等
     */
    public static String getActiveProfile() {
        Environment env = applicationContext.getBean(Environment.class);
        return env.getActiveProfiles()[0];
    }
}