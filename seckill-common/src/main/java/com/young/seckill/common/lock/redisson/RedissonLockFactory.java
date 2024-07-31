package com.young.seckill.common.lock.redisson;

import com.young.seckill.common.lock.DistributedLock;
import com.young.seckill.common.lock.factory.DistributedLockFactory;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(value = "distributed.lock.type", havingValue = "redisson")
public class RedissonLockFactory implements DistributedLockFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonLockFactory.class);

    private final RedissonClient redissonClient;

    public RedissonLockFactory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public DistributedLock getDistributedLock(String key) {

        RLock rLock = redissonClient.getLock(key);

        return new DistributedLock() {
            @Override
            public boolean tryLock(Long waitTime, Long leaseTime, TimeUnit unit) throws InterruptedException {
                boolean isLockSuccess = rLock.tryLock(waitTime, leaseTime, unit);
                LOGGER.info("{} get lock result: {}", key, isLockSuccess);
                return isLockSuccess;
            }

            @Override
            public boolean tryLock() {
                return rLock.tryLock();
            }

            @Override
            public void lock(Long leaseTime, TimeUnit unit) {
                rLock.lock(leaseTime, unit);
            }

            @Override
            public void unlock() {
                if (isLocked() && isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            }

            @Override
            public boolean isLocked() {
                return rLock.isLocked();
            }

            @Override
            public boolean isHeldByThread(Long threadId) {
                return rLock.isHeldByThread(threadId);
            }

            @Override
            public boolean isHeldByCurrentThread() {
                return rLock.isHeldByCurrentThread();
            }
        };
    }
}

