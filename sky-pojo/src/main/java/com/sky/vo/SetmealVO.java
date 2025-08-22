package com.sky.vo;

import com.sky.entity.SetmealDish;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "套餐信息展示VO")
public class SetmealVO implements Serializable {

    @Schema(description = "套餐ID", example = "1")
    private Long id;

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "套餐名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "豪华套餐")
    private String name;

    @Schema(description = "套餐价格", example = "99.99")
    private BigDecimal price;

    @Schema(description = "套餐状态，0停用，1启用", example = "1")
    private Integer status;

    @Schema(description = "套餐描述信息", example = "这是一个豪华套餐，包含多种美食")
    private String description;

    @Schema(description = "套餐图片")
    private String image;

    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "套餐分类名称", example = "商务套餐")
    private String categoryName;

    @Schema(description = "套餐菜品列表")
    private List<SetmealDish> setmealDishes = new ArrayList<>();
}
