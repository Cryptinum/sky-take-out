package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.sky.constant.RedisConstant.SETMEAL_CATEGORY;

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

    @Autowired
    private DishMapper dishMapper;

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
        // 设置实体类中没有的其他字段
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
        // 设置VO中无法从实体类直接获取的字段，分别从category和setmeal_dish表中查询
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
    @CacheEvict(cacheNames = SETMEAL_CATEGORY, key = "#setmealDTO.categoryId")
    public Integer saveSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        int success = setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes == null || setmealDishes.isEmpty()) {
            return success;
        }
        // 前端传来的DTO不含有setmealId，需要手动设置
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
    @CacheEvict(cacheNames = SETMEAL_CATEGORY, allEntries = true)
    public Integer updateSetmealStatus(Integer status, Long id) {
        return setmealMapper.update(new LambdaUpdateWrapper<Setmeal>()
                .eq(Setmeal::getId, id)
                .set(Setmeal::getStatus, status));
    }

    /**
     * 修改套餐信息
     *
     * @param setmealDTO
     * @return
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = SETMEAL_CATEGORY, allEntries = true)
    public Integer editSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        Long setmealId = setmeal.getId();
        int success = setmealMapper.updateById(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        // 先删除原有的套餐菜品
        setmealDishMapper.delete(new LambdaUpdateWrapper<SetmealDish>()
                .in(SetmealDish::getSetmealId, setmealId));
        // 再插入新的套餐菜品
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
            setmealDishMapper.insert(setmealDishes);
        }
        return success;
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = SETMEAL_CATEGORY, allEntries = true)
    public Integer deleteSetmeal(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.ID_LIST_IS_NULL);
        }
        List<Setmeal> setmeals = setmealMapper.selectList(new LambdaQueryWrapper<Setmeal>()
                .in(Setmeal::getId, ids)
                .eq(Setmeal::getStatus, 0));
        if (setmeals.size() != ids.size()) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        // 删除套餐和套餐内菜品
        int i = setmealMapper.deleteByIds(ids);
        int j = setmealDishMapper.delete(new LambdaUpdateWrapper<SetmealDish>()
                .in(SetmealDish::getSetmealId, ids));
        return i + j;
    }

    /**
     * 根据分类ID查询套餐信息
     * @param categoryId
     * @return
     */
    @Override
    @Cacheable(cacheNames = SETMEAL_CATEGORY, key = "#categoryId")
    public List<Setmeal> getSetmealByCategoryId(Long categoryId) {
        return setmealMapper.selectList(new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getCategoryId, categoryId));
    }

    /**
     * 根据套餐ID查询套餐内菜品信息
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishesBySetmealId(Long id) {
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId, id));
        if (setmealDishes == null || setmealDishes.isEmpty()) {
            return List.of();
        }

        List<DishItemVO> dishItemVOS = new ArrayList<>();
        for (SetmealDish setmealDish : setmealDishes) {
            Dish dish = dishMapper.selectById(setmealDish.getDishId());
            if (dish != null) {
                DishItemVO dishItemVO = BeanUtil.copyProperties(dish, DishItemVO.class);
                dishItemVO.setDescription(dishItemVO.getDescription());
                dishItemVO.setImage(dishItemVO.getImage());
                dishItemVOS.add(dishItemVO);
            }
        }
        return dishItemVOS;
    }
}
