package com.sky.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "用户实体", description = "用户实体")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "微信用户唯一标识")
    private String openid;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "性别，0女，1男", example = "1")
    private String sex;

    @Schema(description = "身份证号", example = "110101199003074512")
    private String idNumber;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "注册时间", example = "2023-01-01 12:00:00")
    private LocalDateTime createTime;
}
