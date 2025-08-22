package com.sky.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐菜品关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "套餐菜品关系实体类", description = "表示套餐与菜品之间的关系")
public class SetmealDish implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "套餐菜品关系ID", example = "1")
    private Long id;

    @Schema(description = "套餐ID", example = "1")
    private Long setmealId;

    @Schema(description = "菜品ID", example = "1")
    private Long dishId;

    @Schema(description = "菜品名称", example = "东坡肘子")
    private String name;

    @Schema(description = "菜品原价", example = "10.00")
    private BigDecimal price;

    @Schema(description = "菜品份数", example = "2")
    private Integer copies;
}
