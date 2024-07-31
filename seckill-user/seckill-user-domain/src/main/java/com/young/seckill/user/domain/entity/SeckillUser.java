package com.young.seckill.user.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 6598302397015981790L;

    // 用户ID
    private Long id;

    // 用户名
    private String username;

    // 用户密码
    private String password;

    // 手机号码
    private String phone;

    // 用户状态【1-正常；-1-冻结】
    private Integer status;

    // 创建时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
