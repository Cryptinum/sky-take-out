package com.sky.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 地址簿
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "地址簿实体类")
public class AddressBook implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "用户id", example = "1")
    private Long userId;

    @Schema(description = "收货人", example = "张三")
    private String consignee;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "性别 0 女 1 男", example = "1")
    private String sex;

    @Schema(description = "省级区划编号", example = "11")
    private String provinceCode;

    @Schema(description = "省级名称", example = "北京市")
    private String provinceName;

    @Schema(description = "市级区划编号", example = "1101")
    private String cityCode;

    @Schema(description = "市级名称", example = "北京市")
    private String cityName;

    @Schema(description = "区级区划编号", example = "110101")
    private String districtCode;

    @Schema(description = "区级名称", example = "东城区")
    private String districtName;

    @Schema(description = "详细地址", example = "东华门街道东长安街1号")
    private String detail;

    @Schema(description = "标签", example = "家")
    private String label;

    @Schema(description = "是否默认 0否 1是", example = "1")
    private Integer isDefault;
}
