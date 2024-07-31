package com.young.seckill.user.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.young.seckill")
@MapperScan(basePackages = "com.young.seckill.user.infrastructure.mapper")
public class SeckillUserStarter {

    public static void main(String[] args) {
        SpringApplication.run(SeckillUserStarter.class, args);
    }
}
