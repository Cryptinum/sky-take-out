package com.sky.service.impl;

import com.sky.constant.RedisConstant;
import com.sky.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.26 22:25
 */

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 更新店铺营业状态
     *
     * @param status
     * @return
     */
    @Override
    public Integer updateShopStatus(Integer status) {
        redisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS_KEY, status);
        return status;
    }

    @Override
    public Integer getShopStatus() {
        return (Integer) redisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS_KEY);
    }
}
