package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author FragrantXue
 * @email Create by 2025.08.22 14:12
 */

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据套餐ID查询套餐信息
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    public SetmealVO getSetmealById(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        SetmealVO setmealVO = BeanUtil.copyProperties(setmeal, SetmealVO.class);
        Long setmealId = setmeal.getId();
        Long categoryId = setmeal.getCategoryId();
        Category category = categoryMapper.selectById(categoryId);
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId, setmealId));
        setmealVO.setCategoryName(category.getName());
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 分页查询套餐信息
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    @Transactional
    public PageResult<SetmealVO> getSetmealPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        int page = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();
        String name = setmealPageQueryDTO.getName();
        Integer status = setmealPageQueryDTO.getStatus();
        Integer categoryId = setmealPageQueryDTO.getCategoryId();
        Page<Setmeal> p = Page.of(page, pageSize);
        p.addOrder(OrderItem.desc("update_time"));
        lambdaQuery()
                .eq(categoryId != null, Setmeal::getCategoryId, categoryId)
                .eq(status != null, Setmeal::getStatus, status)
                .like(name != null, Setmeal::getName, name)
                .page(p);
        List<Setmeal> records = p.getRecords();
        if (records == null || records.isEmpty()) {
            return PageResult.empty(p);
        }
        List<SetmealVO> setmealVOList = BeanUtil.copyToList(records, SetmealVO.class);
        for (SetmealVO setmealVO : setmealVOList) {
            setmealVO.setCategoryName(categoryMapper.selectById(setmealVO.getCategoryId()).getName());
            setmealVO.setSetmealDishes(setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>()
                    .eq(SetmealDish::getSetmealId, setmealVO.getId())));
        }
        return new PageResult<>(p.getTotal(), p.getPages(), setmealVOList);
    }

    /**
     * 新增套餐
     *
     * @param setmealDTO
     * @return
     */
    @Override
    @Transactional
    public Integer saveSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        int success = setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes == null || setmealDishes.isEmpty()) {
            return success;
        }
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insert(setmealDishes);
        return success;
    }

    /**
     * 修改套餐状态
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    public Integer updateSetmealStatus(Integer status, Long id) {
        return setmealMapper.update(new LambdaUpdateWrapper<Setmeal>()
                .eq(Setmeal::getId, id)
                .set(Setmeal::getStatus, status));
    }

    @Override
    public Integer editSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        Long setmealId = setmeal.getId();
        int success = setmealMapper.updateById(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishMapper.delete(new LambdaUpdateWrapper<SetmealDish>()
                .in(SetmealDish::getSetmealId, setmealId));
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
            setmealDishMapper.insert(setmealDishes);
        }
        return success;
    }

}
