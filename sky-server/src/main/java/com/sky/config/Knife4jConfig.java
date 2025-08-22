package com.sky.config;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.17 23:08
 */
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class Knife4jConfig {

    /**
     * 配置 API 的基本信息，对应 YAML 中的 knife4j.openapi 部分
     * @return OpenAPI Bean
     */
    @Bean
    public OpenAPI customOpenAPI() {
        log.info("准备生成接口文档...");

        // 1. 创建联系人信息 (Contact)
        Contact contact = new Contact()
                .name("Cryptinum") // 对应 YAML 中的 concat (注意：原 YAML key 'concat' 可能是 'contact' 的笔误)
                .email("qwertyzacium@outlook.com")
                .url("https://github.com/Cryptinum");

        // 2. 创建 API 信息 (Info)
        Info info = new Info()
                .title("苍穹外卖项目接口文档") // 对应 YAML 中的 title
                .description("苍穹外卖项目接口文档") // 对应 YAML 中的 description
                .version("2.0") // 对应 YAML 中的 version
                .contact(contact);

        // 3. 创建 OpenAPI 对象，并设置 API 信息
        return new OpenAPI().info(info);
    }

    /**
     * 配置 API 分组，对应 YAML 中的 knife4j.group 部分
     * @return GroupedOpenApi Bean
     */
    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                // 组名，会显示在UI的下拉列表中
                .group("default") // 对应 YAML 中的 group-name
                // 扫描指定的包，生成接口文档
                .packagesToScan("com.sky.controller") // 对应 YAML 中的 api-rule-resources
                .build();
    }
}
