package com.sky.controller.user;

import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.28 01:52
 */

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Tag(name = "套餐接口", description = "提供套餐接口的一系列功能")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/list")
    @Operation(summary = "根据套餐ID查询套餐信息", description = "提供根据套餐ID查询套餐详细信息的功能")
    public Result<List<Setmeal>> getSetmealByCategoryId(Long categoryId) {
        log.info("查询套餐信息，categoryId: {}", categoryId);
        List<Setmeal> setmealDTO = setmealService.getSetmealByCategoryId(categoryId);
        return Result.success(setmealDTO);
    }

    @GetMapping("/dish/{id}")
    @Operation(summary = "根据套餐id查询包含的菜品", description = "提供根据套餐id查询包含的菜品信息的功能")
    public Result<List<DishItemVO>> getDishesBySetmealId(@PathVariable Long id) {
        log.info("根据套餐id查询包含的菜品, id: {}", id);
        List<DishItemVO> dishItemVO = setmealService.getDishesBySetmealId(id);
        return Result.success(dishItemVO);
    }
}
