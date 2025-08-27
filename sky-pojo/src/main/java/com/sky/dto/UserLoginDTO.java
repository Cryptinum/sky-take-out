package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 */
@Data
@Schema(name = "UserLoginDTO", description = "C端用户登录DTO")
public class UserLoginDTO implements Serializable {

    @Schema(description = "用户身份标识码")
    private String code;

}
