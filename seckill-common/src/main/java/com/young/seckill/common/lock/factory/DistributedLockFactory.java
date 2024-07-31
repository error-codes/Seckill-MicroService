package com.young.seckill.common.lock.factory;

import com.young.seckill.common.lock.DistributedLock;

public interface DistributedLockFactory {

    DistributedLock getDistributedLock(String key);
}
