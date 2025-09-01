package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.31 23:52
 */

@Service
@Slf4j
public class ShoppingCartServiceImpl
        extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
        implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     * @return
     */
    @Override
    @Transactional
    public Integer addShoppingCartItem(ShoppingCartDTO shoppingCartDTO) {
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();
        Long userId = BaseContext.getCurrentId();

        // 先做一个查询，根据id和口味查询到的购物车item一定是唯一的
        ShoppingCart shoppingCartItem = shoppingCartMapper.selectOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(setmealId != null, ShoppingCart::getSetmealId, setmealId)
                .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor)
        );

        // 如果当前加入的购物车中的商品已经存在，则更新商品数量
        if (shoppingCartItem != null) {
            log.info("当前商品已经存在于购物车中，更新商品数量, shoppingCartItems: {}", shoppingCartItem);
            Integer number = shoppingCartItem.getNumber();
            shoppingCartItem.setNumber(number + 1); // 数量加1
            shoppingCartMapper.updateById(shoppingCartItem);
        }

        // 如果不存在，那么需要插入一条购物车数据
        else {
            log.info("当前商品不存在于购物车中，插入一条新的购物车数据, shoppingCartDTO: {}", shoppingCartDTO);
            ShoppingCart shoppingCart = new ShoppingCart();
            Dish dish;
            Setmeal setmeal;
            // 如果添加的是菜品
            if (dishId != null) {
                dish = dishMapper.selectById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            }
            // 如果添加的是套餐
            else {
                setmeal = setmealMapper.selectById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }
            shoppingCart.setUserId(userId);
            shoppingCart.setDishId(dishId);
            shoppingCart.setSetmealId(setmealId);
            shoppingCart.setDishFlavor(dishFlavor);
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            // 插入成功，返回1
            return shoppingCartMapper.insert(shoppingCart);
        }

        // 插入失败，返回0，事务回滚
        return 0;
    }
}
