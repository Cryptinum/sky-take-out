package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "员工分页查询DTO实体")
public class EmployeePageQueryDTO implements Serializable {

    //员工姓名
    @Schema(description = "员工姓名", example = "张三", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    //页码
    @Schema(description = "页码", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private int page;

    //每页显示记录数
    @Schema(description = "每页显示记录数", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private int pageSize;

}
