package com.sky.service;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.26 22:24
 */

public interface ShopService {

    /**
     * 更新店铺营业状态
     *
     * @param status
     * @return
     */
    Integer updateShopStatus(Integer status);

    /**
     * 获取店铺营业状态
     *
     * @return
     */
    Integer getShopStatus();
}
