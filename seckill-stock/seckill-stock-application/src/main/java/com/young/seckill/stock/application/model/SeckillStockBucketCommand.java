package com.young.seckill.stock.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillStockBucketCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = 630664369380015278L;

    // 总库存
    private Integer totalStock;

    // 分桶数量
    private Integer bucketsQuantity;

    // 编排模式【1-总量模式；2-增量模式】
    private Integer arrangementMode;

    public boolean isEmpty() {
        return this.totalStock == null || this.bucketsQuantity == null || this.arrangementMode == null;
    }
}
