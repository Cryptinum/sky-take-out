package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "员工DTO实体")
public class EmployeeDTO implements Serializable {

    @Schema(description = "员工ID", example = "114514")
    private Long id;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "zhangsan")
    private String username;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String name;

    @Schema(description = "电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    private String phone;

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.REQUIRED, example = "男")
    private String sex;

    @Schema(description = "身份证号", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456789012345678")
    private String idNumber;

}
