package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Zhao Chonghao
 * Create by 2025.08.21 23:52
 */

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishMapper dishMapper;


    /**
     * 根据菜品id查询菜品信息
     * @param id
     * @return
     */
    @Override
    public DishVO getDishById(Long id) {
        Dish dish = dishMapper.selectById(id);
        return BeanUtil.copyProperties(dish, DishVO.class);
    }
}
