package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.31 23:47
 */

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Tag(name = "用户端购物车相关接口", description = "提供用户端购物车相关接口的功能")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @Operation(summary = "添加购物车", description = "添加购物车")
    public Result<Integer> addShoppingCartItem(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车, shoppingCartDTO: {}", shoppingCartDTO);
        Integer success = shoppingCartService.addShoppingCartItem(shoppingCartDTO);
        return Result.success(success);
    }
}
