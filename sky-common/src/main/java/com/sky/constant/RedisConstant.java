package com.sky.constant;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.28 17:59
 */

public class RedisConstant {

    public static final String ROOT_PATH = "sky:";
    public static final String CACHE_PATH = ROOT_PATH + "cache:";

    // 持久化
    public static final String SHOP_STATUS_KEY = ROOT_PATH + "shop_status";

    // 缓存对象
    public static final String DISH_CATEGORY_PATH = CACHE_PATH + "dish_category:";

}
