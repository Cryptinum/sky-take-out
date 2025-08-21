package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "菜品分页查询传输对象")
public class DishPageQueryDTO implements Serializable {

    @Schema(description = "页码", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private int page;

    @Schema(description = "每页显示记录数", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private int pageSize;

    @Schema(description = "菜品名称", example = "东坡肘子")
    private String name;

    @Schema(description = "菜品分类ID", example = "11")
    private Integer categoryId;

    @Schema(description = "菜品状态，0停售，1起售", example = "1")
    private Integer status;

}
