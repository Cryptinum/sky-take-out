package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/list")
    @Operation(summary = "查询购物车", description = "查询购物车")
    public Result<List<ShoppingCart>> getShoppingCartItems(){
        log.info("查询购物车");
        return Result.success(shoppingCartService.getShoppingCartItems());
    }

    @PostMapping("/add")
    @Operation(summary = "添加购物车", description = "添加购物车")
    public Result<Integer> addShoppingCartItem(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车, shoppingCartDTO: {}", shoppingCartDTO);
        Integer success = shoppingCartService.addShoppingCartItem(shoppingCartDTO);
        return Result.success(success);
    }

    @PostMapping("/sub")
    @Operation(summary = "删除购物车", description = "删除购物车")
    public Result<Integer> deleteShoppingCartItem(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车, shoppingCartDTO: {}", shoppingCartDTO);
        Integer success = shoppingCartService.deleteShoppingCartItem(shoppingCartDTO);
        return Result.success(success);
    }

    @DeleteMapping("/clean")
    @Operation(summary = "清空购物车", description = "清空购物车")
    public Result<Integer> clearShoppingCart() {
        log.info("清空购物车");
        Integer success = shoppingCartService.clearShoppingCart();
        return Result.success(success);
    }
}
