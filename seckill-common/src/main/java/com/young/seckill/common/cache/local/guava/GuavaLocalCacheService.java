package com.young.seckill.common.cache.local.guava;

import com.google.common.cache.Cache;
import com.young.seckill.common.cache.local.LocalCacheService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "local.cache.type", havingValue = "guava")
public class GuavaLocalCacheService<K, V> implements LocalCacheService<K, V> {

    private final Cache<K, V> cache = LocalCacheFactory.getLocalCache();

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V getIfPresent(Object key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void delete(K key) {
        cache.invalidate(key);
    }
}

