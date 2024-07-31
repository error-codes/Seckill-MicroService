package com.young.seckill.common.cache.distribute.redis;

import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.response.RespCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(value = "distributed.cache.type", havingValue = "redis")
public class RedisDistributedCacheService implements DistributedCacheService {

    private static final DefaultRedisScript<Long> DECREASE_STOCK_SCRIPT;
    private static final DefaultRedisScript<Long> INCREASE_STOCK_SCRIPT;
    private static final DefaultRedisScript<Long> INIT_STOCK_SCRIPT;
    private static final DefaultRedisScript<Long> CHECK_RECOVER_STOCK_SCRIPT;
    private static final DefaultRedisScript<Long> TAKE_ORDER_TOKEN_SCRIPT;
    private static final DefaultRedisScript<Long> CHECK_RECOVER_ORDER_TOKEN_SCRIPT;

    static {
        // 扣减库存
        DECREASE_STOCK_SCRIPT = new DefaultRedisScript<>();
        DECREASE_STOCK_SCRIPT.setLocation(new ClassPathResource("decrement_goods_stock.lua"));
        DECREASE_STOCK_SCRIPT.setResultType(Long.class);

        // 增加库存
        INCREASE_STOCK_SCRIPT = new DefaultRedisScript<>();
        INCREASE_STOCK_SCRIPT.setLocation(new ClassPathResource("increment_goods_stock.lua"));
        INCREASE_STOCK_SCRIPT.setResultType(Long.class);

        // 初始化库存
        INIT_STOCK_SCRIPT = new DefaultRedisScript<>();
        INIT_STOCK_SCRIPT.setLocation(new ClassPathResource("init_goods_stock.lua"));
        INIT_STOCK_SCRIPT.setResultType(Long.class);

        // 检测是否执行过恢复缓存库存的操作
        CHECK_RECOVER_STOCK_SCRIPT = new DefaultRedisScript<>();
        CHECK_RECOVER_STOCK_SCRIPT.setLocation(new ClassPathResource("check_recover_stock.lua"));
        CHECK_RECOVER_STOCK_SCRIPT.setResultType(Long.class);

        // 获取下单许可
        TAKE_ORDER_TOKEN_SCRIPT = new DefaultRedisScript<>();
        TAKE_ORDER_TOKEN_SCRIPT.setLocation(new ClassPathResource("take_order_token.lua"));
        TAKE_ORDER_TOKEN_SCRIPT.setResultType(Long.class);

        // 检测是否执行过恢复下单许可
        CHECK_RECOVER_ORDER_TOKEN_SCRIPT = new DefaultRedisScript<>();
        CHECK_RECOVER_ORDER_TOKEN_SCRIPT.setLocation(new ClassPathResource("check_recover_order_token.lua"));
        CHECK_RECOVER_ORDER_TOKEN_SCRIPT.setResultType(Long.class);
    }

    private final RedisTemplate<String, Object> redisTemplate;

    @Lazy
    public RedisDistributedCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(String key, String value) {
        if (StringUtils.hasLength(key) || value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void put(String key, Object value) {
        if (!StringUtils.hasLength(key) || value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void put(String key, Object value, long expireTime) {
        if (!StringUtils.hasLength(key) || value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
    }

    @Override
    public void put(String key, Object value, long expireTime, TimeUnit timeUnit) {
        if (!StringUtils.hasLength(key) || value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
    }

    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        Object result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }

        return clazz.cast(result);
    }

    @Override
    public String getString(String key) {
        Object result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }

        return result.toString();
    }

    @Override
    public Boolean delete(String key) {
        if (StringUtils.hasLength(key)) {
            return false;
        }
        return redisTemplate.delete(key);
    }

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Long addSet(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    @Override
    public Long removeSet(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    @Override
    public Boolean isMemberSet(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    @Override
    public Long decrement(String key, long count) {
        return redisTemplate.opsForValue().decrement(key, count);
    }

    @Override
    public Long increment(String key, long count) {
        return redisTemplate.opsForValue().increment(key, count);
    }

    @Override
    public Long decrementByLua(String key, Integer quantity) {
        return redisTemplate.execute(DECREASE_STOCK_SCRIPT, Collections.singletonList(key), quantity);
    }

    @Override
    public Long incrementByLua(String key, Integer quantity) {
        return redisTemplate.execute(INCREASE_STOCK_SCRIPT, Collections.singletonList(key), quantity);
    }

    @Override
    public Long initByLua(String key, Integer quantity) {
        return redisTemplate.execute(INIT_STOCK_SCRIPT, Collections.singletonList(key), quantity);
    }

    @Override
    public void checkResultByLua(Long result) {
        ExceptionChecker.throwAssertIfEqual(result, SeckillConstants.LUA_GOODS_STOCK_NOT_EXIST, RespCode.STOCK_IS_NULL);
        ExceptionChecker.throwAssertIfEqual(result, SeckillConstants.LUA_GOODS_QUANTITY_LT_ZERO, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfEqual(result, SeckillConstants.LUA_GOODS_STOCK_GT_ZERO, RespCode.STOCK_LT_ZERO);
    }

    @Override
    public Long checkRecoverStockByLua(String key, Long seconds) {
        return redisTemplate.execute(CHECK_RECOVER_STOCK_SCRIPT, Collections.singletonList(key), seconds);
    }

    @Override
    public Long takeOrderToken(String key) {
        return redisTemplate.execute(TAKE_ORDER_TOKEN_SCRIPT, Collections.singletonList(key));
    }

    @Override
    public Long checkRecoverOrderToken(String key) {
        return redisTemplate.execute(CHECK_RECOVER_ORDER_TOKEN_SCRIPT, Collections.singletonList(key));
    }
}