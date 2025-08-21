package com.sky.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜品分类实体")
public class Category implements Serializable {

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

    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID", example = "1")
    private Long createUser;

    @Schema(description = "修改人ID", example = "1")
    private Long updateUser;
}
