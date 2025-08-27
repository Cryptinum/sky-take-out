package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.26 17:45
 */

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Tag(name = "店铺接口", description = "店铺接口信息相关接口")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @GetMapping("/status")
    @Operation(summary = "获取营业状态", description = "获取店铺的营业状态")
    public Result<Integer> getShopStatus() {
        Integer status = shopService.getShopStatus();
        log.info("获取店铺营业状态为: {}", status);
        return Result.success(status);
    }

    @PutMapping("/{status}")
    @Operation(summary = "设置营业状态", description = "设置店铺的营业状态")
    public Result<Integer> updateShopStatus(@PathVariable Integer status) {
        log.info("修改营业状态为: {}", status);
        Integer success = shopService.updateShopStatus(status);
        return Result.success(success);
    }
}
