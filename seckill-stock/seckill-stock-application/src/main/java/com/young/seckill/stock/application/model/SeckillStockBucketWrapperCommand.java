package com.young.seckill.stock.application.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class SeckillStockBucketWrapperCommand extends SeckillStockBucketGoodsCommand {

    @Serial
    private static final long serialVersionUID = 5508711329860605398L;

    // 库存分桶信息
    private SeckillStockBucketCommand seckillStockBucketCommand;

    public SeckillStockBucketWrapperCommand(Long userId, Long goodsId, SeckillStockBucketCommand seckillStockBucketCommand) {
        super(userId, goodsId);
        this.seckillStockBucketCommand = seckillStockBucketCommand;
    }

    public boolean isEmpty() {
        return this.seckillStockBucketCommand == null || super.isEmpty() || seckillStockBucketCommand.isEmpty();
    }
}
