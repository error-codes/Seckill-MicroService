package com.young.seckill.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class SnowFlakeID {

    // 起始的时间戳 2023-12-07 00:00:00  毫秒
    private final static long START_STAMP = 1701878400000L;


    // 每一部分占用的位数
    private final static long SEQUENCE_BIT   = 12; // 序列号占用的位数
    private final static long MACHINE_BIT    = 5;  // 机器标识占用的位数
    private final static long DATACENTER_BIT = 5;  // 数据中心占用的位数


    // 每一部分最大值
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT); // 最大数据中心数 31
    private final static long MAX_MACHINE_NUM    = ~(-1L << MACHINE_BIT);    // 最大机器数 31
    private final static long MAX_SEQUENCE       = ~(-1L << SEQUENCE_BIT);   // 最大序列号数 4095


    // 每一部分左移位数
    private final static long            MACHINE_LEFT         = SEQUENCE_BIT;                     // 机器数左移 12 位
    private final static long            DATACENTER_LEFT      = MACHINE_LEFT + MACHINE_BIT;       // 数据中心数左移 17 位
    private final static long            TIMESTAMP_LEFT       = DATACENTER_LEFT + DATACENTER_BIT; // 时间戳左移 22位
    // 最大容忍时间, 单位毫秒, 即如果时钟只是回拨了该变量指定的时间, 那么等待相应的时间即可;
    // 考虑到sequence服务的高性能, 这个值不易过大
    private static final long            MAX_BACKWARD_MS      = 5;
    // 保留machineId和lastTimestamp, 以及备用machineId和其对应的lastTimestamp
    private static final Map<Long, Long> machineIdLastTimeMap = new ConcurrentHashMap<>();
    private final        long            datacenterId;      // 数据中心
    private              long            machineId;         // 机器标识
    private              long            sequence             = 0L;    // 序列号
    private              long            lastStamp            = -1L;   // 上一次时间戳

    /**
     * 初始化数据中心位，和机器标识
     * 0 < datacenterId < MAX_DATACENTER_NUM 31
     * 0 < machineId < MAX_MACHINE_NUM 31
     */
    public SnowFlakeID(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException(" datacenterId 必须介于[0,31] ");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException(" machineId 必须介于[0,31] ");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
        // 初始化时间 machineIdLastTimeMap
        machineIdLastTimeMap.put(machineId, System.currentTimeMillis());
    }

    /**
     * 产生下一个ID
     */
    public synchronized Long nextId() {
        // 现存的扩展字段
        long extension = 0L;
        // 获取当前时间毫秒数
        long currentStamp = System.currentTimeMillis();

        // lastStamp = currentStamp + 100;
        if (currentStamp < lastStamp) {
            // 如果时钟回拨在可接受范围内, 等待即可
            long offset = lastStamp - currentStamp;
            // 如果回拨时间不超过5毫秒，就等待相应的时间

            if (offset <= MAX_BACKWARD_MS) {
                // 休眠（lastTimestamp - currentTimestamp）毫秒，让其追上
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(offset));

                currentStamp = System.currentTimeMillis();

                // 如果时间还小于当前时间，那么利用扩展字段加 1
                if (currentStamp < lastStamp) {
                    //扩展字段
                    extension += 1;
                }
            } else {
                // 扩展字段
                extension += 1;
                // 获取可以用的 workId，对应的时间戳，必须大于当前时间戳
                tryGenerateKeyOnBackup(currentStamp);
            }
        }

        if (currentStamp == lastStamp) {
            // 相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currentStamp = getNextMill();
            }
        } else {
            // 不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStamp = currentStamp;

        return (currentStamp - START_STAMP) << (TIMESTAMP_LEFT - extension)     // 时间戳部分
               | datacenterId << DATACENTER_LEFT                               // 数据中心部分
               | machineId << MACHINE_LEFT                                     // 机器标识部分
               | sequence;                                                     // 序列号部分
    }

    /**
     * 自旋锁获取当前时间戳
     */
    private long getNextMill() {
        long mill = System.currentTimeMillis();
        while (mill <= lastStamp) {
            mill = System.currentTimeMillis();
        }
        return mill;
    }

    /**
     * 尝试在 machineId 的备份 machineId 上生成
     * 核心优化代码在方法 tryGenerateKeyOnBackup()中
     * BACKUP_COUNT 即备份 machineId
     * 统计数越多，sequence 服务避免时钟回拨影响的能力越强，但是可部署的sequence服务越少，
     * 设置 BACKUP_COUNT 为3，最多可以部署 1024 / (3+1) 即 256 个 sequence 服务，完全够用
     * 抗时钟回拨影响的能力也得到非常大的保障。
     */
    private void tryGenerateKeyOnBackup(long currentMillis) {
        // 遍历所有machineId(包括备用 machineId, 查看哪些 machineId 可用)
        for (Map.Entry<Long, Long> entry : machineIdLastTimeMap.entrySet()) {
            this.machineId = entry.getKey();
            // 取得备用 machineId 的 lastTime
            Long tempLastTime = entry.getValue();
            lastStamp = tempLastTime == null ? 0L : tempLastTime;

            // 如果找到了合适的 machineId，返回合适的时间，
            if (lastStamp <= currentMillis) {
                return;
            }
        }

        // 如果所有 machineId 以及备用 machineId 都处于时钟回拨, 那么抛出异常
        throw new IllegalArgumentException(
                "时钟在向后移动，当前时间是 " + currentMillis + " 毫秒，machineId映射 = " + machineIdLastTimeMap);
    }

}

