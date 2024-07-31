package com.young.seckill.stock.application.event;

import com.young.seckill.common.response.RespResult;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.stock.application.cache.SeckillStockBucketCacheService;
import com.young.seckill.stock.domain.enums.SeckillStockBucketEventType;
import com.young.seckill.stock.domain.event.SeckillStockBucketEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "event.publish.type", havingValue = "spring")
public class SeckillStockEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillStockEventListener.class);

    private final SeckillStockBucketCacheService seckillStockBucketCacheService;

    public SeckillStockEventListener(SeckillStockBucketCacheService seckillStockBucketCacheService) {
        this.seckillStockBucketCacheService = seckillStockBucketCacheService;
    }

    @EventListener
    public RespResult<String> execute(SeckillStockBucketEvent event) {
        LOGGER.info("Spring|StockEvent 接收库存事件|{}", JACKSON.toJson(event));

        if (event == null || event.getGoodsId() == null) {
            LOGGER.info("Spring|StockEvent|库存参数错误");
            return RespResult.success();
        }

        // 开启了库存分桶，更新缓存数据
        if (SeckillStockBucketEventType.ENABLED.getCode().equals(event.getStatus())) {
            seckillStockBucketCacheService.tryUpdateSeckillStockBucketCacheByLock(event.getGoodsId(), false);
        }

        return RespResult.success();
    }
}
