package com.young.seckill.stock.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = "com.young.seckill")
@MapperScan(value = "com.young.seckill.stock.infrastructure.mapper")
public class SeckillStockStarter {

    public static void main(String[] args) {
        SpringApplication.run(SeckillStockStarter.class, args);
    }

}

