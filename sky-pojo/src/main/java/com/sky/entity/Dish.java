package com.sky.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜品实体类")
public class Dish implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "创建用户ID", example = "1")
    private Long createUser;

    @Schema(description = "更新用户ID", example = "1")
    private Long updateUser;

}
