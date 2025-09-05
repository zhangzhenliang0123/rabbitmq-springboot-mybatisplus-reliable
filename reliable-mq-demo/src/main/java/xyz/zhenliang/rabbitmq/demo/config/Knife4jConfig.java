package xyz.zhenliang.rabbitmq.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 配置类
 * 1. 配置OpenAPI文档的基本信息
 * 2. 按业务模块分组API
 * 3. 注意：需要配合@Tag和@Operation等注解在Controller上使用
 */
@Configuration
public class Knife4jConfig {

    /**
     * 创建OpenAPI配置
     *
     * @return OpenAPI实例
     * @note 可扩展配置项：
     * - servers: 服务器配置
     * - externalDocs: 外部文档
     * - security: 安全配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Demo API")
                        .version("1.0")
                        .description("Demo 项目接口文档 - Knife4j增强")
                        .contact(new Contact().name("技术支持").email("58722247@qq.com"))
                )
                ;
    }

    /**
     * Demo模块API分组
     *
     * @return 分组配置
     * @note 对应路径: /api/demo/**
     */
    @Bean
    public GroupedOpenApi starApi() {
        return createGroupedApi("Demo模块", "/**");
    }

    /**
     * 创建API分组配置的通用方法
     *
     * @param groupName   分组名称(显示在文档中)
     * @param pathToMatch 要匹配的路径模式
     * @return 分组配置
     * @note 可用于快速扩展新模块的分组
     */
    private GroupedOpenApi createGroupedApi(String groupName, String pathToMatch) {
        return GroupedOpenApi.builder()
                .group(groupName)
                .pathsToMatch(pathToMatch)
                .build();
    }
}