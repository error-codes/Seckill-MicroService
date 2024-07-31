package com.young.seckill.goods.application.event;

import com.young.seckill.common.response.RespResult;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.goods.application.cache.service.SeckillGoodsCacheService;
import com.young.seckill.goods.application.cache.service.SeckillGoodsListCacheService;
import com.young.seckill.goods.domain.event.SeckillGoodsEvent;
import com.young.seckill.goods.domain.service.impl.SeckillGoodsDomainServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "event.publish.type", havingValue = "spring")
public class SeckillGoodsEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillGoodsDomainServiceImpl.class);

    private final SeckillGoodsCacheService     seckillGoodsCacheService;
    private final SeckillGoodsListCacheService seckillGoodsListCacheService;

    public SeckillGoodsEventListener(SeckillGoodsCacheService seckillGoodsCacheService,
                                     SeckillGoodsListCacheService seckillGoodsListCacheService) {
        this.seckillGoodsCacheService = seckillGoodsCacheService;
        this.seckillGoodsListCacheService = seckillGoodsListCacheService;
    }

    @EventListener
    public RespResult<String> execute(SeckillGoodsEvent event) {
        LOGGER.info("Spring|GoodsEvent 接收商品事件|{}", JACKSON.toJson(event));

        seckillGoodsCacheService.tryUpdateSeckillGoodsCacheByLock(event.getGoodsId(), false);
        seckillGoodsListCacheService.tryUpdateSeckillGoodsCacheByLock(event.getActivityId(), false);

        return RespResult.success();
    }
}
