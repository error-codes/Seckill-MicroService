package com.young.seckill.goods.application.dubbo;

import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.model.dto.SeckillGoodsDTO;
import com.young.seckill.dubbo.interfaces.goods.SeckillGoodsDubboService;
import com.young.seckill.goods.application.service.SeckillGoodsService;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


@Component
@DubboService(version = "1.0.0")
public class SeckillGoodsDubboServiceImpl implements SeckillGoodsDubboService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillGoodsDubboServiceImpl.class);

    private final SeckillGoodsService     seckillGoodsService;
    private final DistributedCacheService distributedCacheService;

    public SeckillGoodsDubboServiceImpl(@Lazy SeckillGoodsService seckillGoodsService,
                                        DistributedCacheService distributedCacheService) {
        this.seckillGoodsService = seckillGoodsService;
        this.distributedCacheService = distributedCacheService;
    }

    @Override
    public SeckillGoodsDTO getSeckillGoods(Long goodsId, Long version) {
        return seckillGoodsService.getSeckillGoodsById(goodsId, version);
    }

    @Override
    public boolean updateGoodsAvailableStock(Long goodsId, Integer quantity) {
        return seckillGoodsService.updateGoodsAvailableStock(goodsId, quantity);
    }

    @Override
    public Integer getAvailableStockByGoodsId(Long goodsId) {
        return seckillGoodsService.getAvailableStockByGoodsId(goodsId);
    }
}
