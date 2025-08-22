package com.sky.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author FragrantXue
 * Create by 2025.08.21 21:13
 */

@Data
public class BaseEntity implements Serializable {

    @TableField(fill = FieldFill.INSERT) // 插入时填充
    @Schema(description = "创建时间", example = "2023-10-01 12:00:00")
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时都填充
    @Schema(description = "更新时间", example = "2023-10-01 12:00:00")
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT) // 插入时填充
    @Schema(description = "创建人ID", example = "1")
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时都填充
    @Schema(description = "修改人ID", example = "1")
    private Long updateUser;
}
