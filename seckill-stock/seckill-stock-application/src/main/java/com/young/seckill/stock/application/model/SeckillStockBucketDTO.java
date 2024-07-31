package com.young.seckill.stock.application.model;

import com.young.seckill.stock.domain.entity.SeckillStockBucket;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class SeckillStockBucketDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1738471696828868495L;

    // 库存总量
    private Integer totalStock;

    // 可用库存量
    private Integer availableStock;

    // 分桶数量
    private Integer bucketsQuantity;

    // 库存分桶信息
    private List<SeckillStockBucket> buckets;

    public SeckillStockBucketDTO(Integer totalStock, Integer availableStock, List<SeckillStockBucket> buckets) {
        this.totalStock = totalStock;
        this.availableStock = availableStock;
        this.bucketsQuantity = buckets.size();
        this.buckets = buckets;
    }
}
