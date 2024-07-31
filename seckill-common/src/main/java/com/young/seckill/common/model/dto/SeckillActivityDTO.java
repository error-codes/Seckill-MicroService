package com.young.seckill.common.model.dto;

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
public class SeckillActivityDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8896964172467412383L;

    // 活动ID
    private Long id;

    // 活动名称
    private String activityName;

    // 活动开始时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    // 活动结束时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    // 活动状态  0：已发布  1：上线  -1：下线
    private Integer status;

    // 活动描述
    private String activityDesc;

    // 缓存版本
    private Long version;
}
