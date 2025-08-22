package com.sky.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "套餐实体类")
public class Setmeal extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
}
