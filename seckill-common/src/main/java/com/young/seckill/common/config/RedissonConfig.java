package com.young.seckill.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@ConditionalOnProperty(value = "distributed.lock.type", havingValue = "redisson")
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    @ConditionalOnProperty(value = "redis.deploy.type", havingValue = "single")
    public RedissonClient singleRedissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(createRedisAddress(redisHost, redisPort)).setDatabase(0);
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnProperty(value = "redis.deploy.type", havingValue = "cluster")
    public RedissonClient clusterRedissonClient() {
        Config config = new Config();
        ClusterServersConfig clusterServersConfig = config.useClusterServers();
        clusterServersConfig.setNodeAddresses(Collections.singletonList(createRedisAddress(redisHost, redisPort)));
        return Redisson.create(config);
    }

    public String createRedisAddress(String host, int port) {
        return "redis://" + host + ":" + port;
    }
}