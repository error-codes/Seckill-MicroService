package com.young.seckill.common.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillUserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6566222004247129951L;

    @NotBlank(message = "用户名不能为空")
    @Min(value = 5, message = "用户名长度控制在 5 - 12 位")
    @Max(value = 12, message = "用户名长度控制在 5 - 12 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Min(value = 8, message = "密码长度控制在 8 - 15 位")
    @Max(value = 15, message = "密码长度控制在 8 - 15 位")
    private String password;

    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = "^1(3|5|7|8|9\\d{9}$)", message = "手机号格式错误")
    private String phone;
}

