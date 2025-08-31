package com.sky.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import static com.sky.constant.RedisConstant.CACHE_PATH;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.25 01:05
 */

@Configuration
@EnableCaching
@Slf4j
public class RedisConfiguration {

    private StringRedisSerializer stringRedisSerializer;

    private GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer;

    public RedisConfiguration() {
        this.stringRedisSerializer = new StringRedisSerializer();
        this.genericJackson2JsonRedisSerializer = buildRedisSerializer();
    }

    /**
     * 创建RedisTemplate模板类
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建Redis模板类");

        // 默认使用的是lettuce连接工厂
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 设置key的序列化器，默认为JdkSerializationRedisSerializer
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        // 设置value的序列化器，默认为JdkSerializationRedisSerializer
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    /**
     * 创建Redis缓存管理器
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 1. 创建 RedisCacheConfiguration Bean，自定义缓存配置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 2. 设置键的序列化器为 StringRedisSerializer
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringRedisSerializer))
                // 3. 设置值的序列化器为 GenericJackson2JsonRedisSerializer
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer))
                // 4. 自定义键的前缀（修改分隔符或添加内容）
                // 方案A：只把分隔符从 "::" 改为 ":"
                // .computePrefixWith(cacheName -> cacheName + ":")

                // 方案B：在分隔符中间加入自定义内容，这正是你想要的
                .computePrefixWith(cacheName -> CACHE_PATH + cacheName + ":")

                // 5. 设置默认缓存过期时间，例如1小时
                .entryTtl(Duration.ofHours(1));

        // 6. 使用自定义的缓存配置初始化 RedisCacheManager
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 构建一个包含 LocalDateTime 模块的 Redis 序列化器
     */
    private GenericJackson2JsonRedisSerializer buildRedisSerializer() {
        // 创建一个自定义的 ObjectMapper
        // 注册 JavaTimeModule 模块来支持 LocalDateTime 等 Java 8 日期类型
        // 设置默认的类型处理，它会在JSON中加入@class属性，指明对象的具体类型，以便在反序列化时能够正确地将JSON转换回相应的Java对象
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
