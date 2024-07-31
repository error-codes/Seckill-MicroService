package com.young.seckill.common.cache.local;

public interface LocalCacheService<K, V> {

    void put(K key, V value);

    V getIfPresent(Object key);

    void delete(K key);
}
