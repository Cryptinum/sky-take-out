package com.sky.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.25 01:05
 */

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建Redis模板类");

        // 默认使用的是lettuce连接工厂
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // 创建一个自定义的 ObjectMapper
        // 注册 JavaTimeModule 模块来支持 LocalDateTime 等 Java 8 日期类型
        // 设置默认的类型处理，它会在JSON中加入@class属性，指明对象的具体类型，以便在反序列化时能够正确地将JSON转换回相应的Java对象
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 设置key的序列化器，默认为JdkSerializationRedisSerializer
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        // 设置value的序列化器，默认为JdkSerializationRedisSerializer
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
