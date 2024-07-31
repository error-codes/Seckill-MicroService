package com.young.seckill.common.utils;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SnowFlakeFactory {

    private static final String DEFAULT_GLOBAL_SNOW_FLAKE = "global_snow_flake";
    private static final ConcurrentHashMap<String, SnowFlakeID> SNOW_FLAKE_ID_CACHE = new ConcurrentHashMap<>();
    private static long datacenterId;      // 数据中心
    private static long machineId;         // 机器标识
    private static SnowFlakeID SNOW_FLAKE_INSTANCE;

    public static void initializeInstance() {
        if (datacenterId != 0 && machineId != 0) {
            SNOW_FLAKE_INSTANCE = new SnowFlakeID(datacenterId, machineId);
        }
    }

    public static SnowFlakeID getSnowFlakeIDCache() {
        return SNOW_FLAKE_ID_CACHE.computeIfAbsent(DEFAULT_GLOBAL_SNOW_FLAKE, k -> SNOW_FLAKE_INSTANCE);
    }

    public static SnowFlakeID getSnowFlakeIDCache(String businessName) {
        return SNOW_FLAKE_ID_CACHE.computeIfAbsent(businessName, k -> new SnowFlakeID(datacenterId, machineId));
    }

    @Value("${snow-flake.dataCenterId}")
    public void setDatacenterId(long datacenterId) {
        SnowFlakeFactory.datacenterId = datacenterId;
        initializeInstance();
    }

    @Value("${snow-flake.machineId}")
    public void setMachineId(long machineId) {
        SnowFlakeFactory.machineId = machineId;
        initializeInstance();
    }
}
