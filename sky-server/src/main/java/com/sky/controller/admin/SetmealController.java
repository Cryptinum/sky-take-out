package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author FragrantXue
 * @email Create by 2025.08.22 14:09
 */

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Tag(name = "套餐接口", description = "提供套餐接口的一系列功能")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/{id}")
    @Operation(summary = "根据套餐ID查询套餐信息", description = "提供根据套餐ID查询套餐详细信息的功能")
    public Result<SetmealVO> getSetmealById(@PathVariable Long id) {
        log.info("查询套餐信息，id: {}", id);
        SetmealVO setmealDTO = setmealService.getSetmealById(id);
        return Result.success(setmealDTO);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询套餐信息", description = "提供分页查询套餐信息的功能")
    public Result<PageResult<SetmealVO>> getSetmealPage(@ParameterObject SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("分页查询套餐信息: {}", setmealPageQueryDTO);
        PageResult<SetmealVO> pageResult = setmealService.getSetmealPage(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    @Operation(summary = "新增套餐", description = "提供新增套餐的功能")
    public Result<Integer> saveSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐: {}", setmealDTO);
        Integer success = setmealService.saveSetmeal(setmealDTO);
        return Result.success(success);
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "修改套餐状态", description = "提供停售启售套餐的功能")
    public Result<Integer> updateSetmealStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("修改套餐状态: status: {}, id: {}", status, id);
        Integer success = setmealService.updateSetmealStatus(status, id);
        return Result.success(success);
    }
}
