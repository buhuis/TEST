package com.sky.constant;

/**
 * Redis key 常量类
 */
public class RedisKeyConstant {

    /**
     * 店铺营业状态（1为营业，0为打烊）
     */
    public static final String SHOP_STATUS = "SHOP_STATUS";

    /**
     * 缓存菜品数据的key前缀（完整key：dish_分类id）
     */
    public static final String DISH_KEY_PREFIX = "dish_";

    /**
     * 缓存套餐数据的key前缀（完整key：setmeal_分类id）
     */
    public static final String SETMEAL_KEY_PREFIX = "setmeal_";

    private RedisKeyConstant() {
    }
}
