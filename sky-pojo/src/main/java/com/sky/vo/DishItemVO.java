package com.sky.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜品项展示VO")
public class DishItemVO implements Serializable {

    @Schema(description = "菜品名称", example = "宫保鸡丁")
    private String name;

    @Schema(description = "份数", example = "2")
    private Integer copies;

    @Schema(description = "菜品图片")
    private String image;

    @Schema(description = "菜品描述", example = "这是一道经典的川菜，口味麻辣鲜香")
    private String description;
}
