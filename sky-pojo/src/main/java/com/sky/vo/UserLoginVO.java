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
@Schema(name = "UserLoginVO", description = "C端用户登录返回VO")
public class UserLoginVO implements Serializable {

    @Schema(description = "用户id")
    private Long id;

    @Schema(description = "微信接口返回的openid")
    private String openid;

    @Schema(description = "jwt令牌，登录状态token")
    private String token;

}
