package com.young.seckill.common.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLock {

    boolean tryLock(Long waitTime, Long leaseTime, TimeUnit unit) throws InterruptedException;

    boolean tryLock();

    void lock(Long leaseTime, TimeUnit unit);

    void unlock();

    boolean isLocked();

    boolean isHeldByThread(Long threadId);

    boolean isHeldByCurrentThread();
}
