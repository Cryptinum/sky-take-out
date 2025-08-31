package com.sky.constant;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.28 17:59
 */

public class RedisConstant {

    public static final String ROOT_PATH = "sky:";
    public static final String CACHE_PATH = ROOT_PATH + "cache:";
    public static final String SHOP_STATUS = "shop_status";

    public static final String DISH_CATEGORY = "dish_category";
    public static final String SETMEAL_CATEGORY = "setmeal_category";

    // 持久化
    public static final String SHOP_STATUS_KEY = ROOT_PATH + SHOP_STATUS;

    // 缓存对象
    public static final String DISH_CATEGORY_KEY = CACHE_PATH + DISH_CATEGORY;
    public static final String SETMEAL_CATEGORY_KEY = CACHE_PATH + SETMEAL_CATEGORY;

}
