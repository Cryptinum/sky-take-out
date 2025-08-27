package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.26 17:45
 */

@RestController("userShopController")
@RequestMapping("/user/shop")
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
}
