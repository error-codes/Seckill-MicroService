package com.young.seckill.common.cache.distribute;

import java.util.concurrent.TimeUnit;

public interface DistributedCacheService {

    void put(String key, String value);

    void put(String key, Object value);

    void put(String key, Object value, long expireTime);

    void put(String key, Object value, long expireTime, TimeUnit timeUnit);

    <T> T getObject(String key, Class<T> clazz);

    String getString(String key);

    Boolean delete(String key);

    Boolean hasKey(String key);

    Long addSet(String key, Object... values);

    Long removeSet(String key, Object... values);

    Boolean isMemberSet(String key, Object value);

    Long decrement(String key, long count);

    Long decrementByLua(String key, Integer quantity);

    Long increment(String key, long count);

    Long incrementByLua(String key, Integer quantity);

    Long initByLua(String key, Integer quantity);

    void checkResultByLua(Long result);

    Long checkRecoverStockByLua(String key, Long seconds);

    Long takeOrderToken(String key);

    Long checkRecoverOrderToken(String key);
}