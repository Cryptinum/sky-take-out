package com.sky.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "category")
@Schema(description = "菜品分类实体")
public class Category extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID", example = "1")
    private Long id;

    @Schema(description = "分类类型 1菜品分类 2套餐分类", example = "1")
    private Integer type;

    @Schema(description = "分类名称", example = "主食")
    private String name;

    @Schema(description = "分类顺序", example = "1")
    private Integer sort;

    @Schema(description = "分类状态 0禁用 1启用", example = "1")
    private Integer status;
}
