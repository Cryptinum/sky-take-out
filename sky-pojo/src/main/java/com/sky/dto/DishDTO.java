package com.sky.dto;

import com.sky.entity.DishFlavor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "菜品信息传输对象")
public class DishDTO implements Serializable {

    @Schema(description = "菜品ID", example = "1")
    private Long id;

    @Schema(description = "菜品名称", example = "东坡肘子")
    private String name;

    @Schema(description = "菜品分类ID", example = "11")
    private Long categoryId;

    @Schema(description = "菜品价格", example = "10.00")
    private BigDecimal price;

    @Schema(description = "菜品图片", example = "https://sky-itcast.oss-cn-beijing.aliyuncs.com/4451d4be-89a2-4939-9c69-3a87151cb979.png")
    private String image;

    @Schema(description = "菜品描述", example = "这是一道非常美味的菜品")
    private String description;

    @Schema(description = "菜品状态，0停售，1起售）", example = "1")
    private Integer status;

    @Schema(description = "菜品口味列表")
    private List<DishFlavor> flavors = new ArrayList<>();

}
