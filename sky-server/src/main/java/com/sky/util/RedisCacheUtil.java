package com.sky.util;

import com.sky.constant.MessageConstant;
import com.sky.exception.DeletionNotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.29 13:18
 */

@Component
@Slf4j
public class RedisCacheUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 使用 SCAN 命令安全地删除指定模式的key
     */
    public void cleanCacheSafe(String keyPattern) {
        // 定义要匹配的key的模式
        Set<String> keysToDelete = new HashSet<>();

        // 使用 execute 方法执行 SCAN 操作
        redisTemplate.execute((RedisConnection connection) -> {
            // 使用 try-with-resources 确保 cursor 被正确关闭
            try (Cursor<byte[]> cursor = connection.keyCommands()
                    .scan(ScanOptions.scanOptions()
                    .match(keyPattern) // 匹配模式
                    .count(1000)    // 每次扫描的数量
                    .build())) {
                while (cursor.hasNext()) {
                    // 找到一个key就添加到待删除集合中
                    keysToDelete.add(new String(cursor.next()));
                }
            } catch (Exception e) {
                throw new DeletionNotAllowedException(MessageConstant.UNKNOWN_ERROR);
            }
            return null;
        });

        // 如果找到了key，则一次性批量删除
        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
            log.info("通过 SCAN 成功删除了 {} 个菜品分类缓存", keysToDelete.size());
        } else {
            log.info("没有找到匹配的菜品分类缓存");
        }
    }
}
