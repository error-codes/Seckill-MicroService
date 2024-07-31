package com.young.seckill.goods.starter;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo(scanBasePackages = "com.young.seckill")
@SpringBootApplication(scanBasePackages = "com.young.seckill")
@MapperScan(value = "com.young.seckill.goods.infrastructure.mapper")
public class SeckillGoodsStarter {

    public static void main(String[] args) {
        SpringApplication.run(SeckillGoodsStarter.class, args);
    }
}


