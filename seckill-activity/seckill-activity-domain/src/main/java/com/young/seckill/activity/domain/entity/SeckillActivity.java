package com.young.seckill.activity.domain.entity;

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
public class SeckillActivity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1743015313929056132L;

    // 活动id
    private Long id;

    // 活动名称
    private String activityName;

    // 活动开始时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    // 活动结束时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    // 活动状态【0-已发布；1-上线；-1-下线】
    private Integer status;

    // 活动描述
    private String activityDesc;

}
