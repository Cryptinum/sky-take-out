package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.31 23:52
 */

public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    Integer addShoppingCartItem(ShoppingCartDTO shoppingCartDTO);
}
