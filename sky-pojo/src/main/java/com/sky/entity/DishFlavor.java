package com.sky.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜品口味
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜品口味实体类")
public class DishFlavor implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "口味ID", example = "50")
    private Long id;

    @Schema(description = "菜品ID", example = "52")
    private Long dishId;

    @Schema(description = "口味名称", example = "甜味")
    private String name;

    @Schema(description = "口味值", example = "[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]")
    private String value;

}
