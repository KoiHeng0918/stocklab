package com.stocklab.benchmark.strategy;

/**
 * 扣库存策略：把“扣库存”从下单主流程里抽出来，方便 E0/E0b/E1/E2/E3 切换。
 */
public interface StockDeductStrategy {

    /**
     * 扣减 1 件库存。
     *
     * @return true=扣减成功；false=库存不足；E2 版本冲突时抛 VersionConflictException
     */
    boolean deduct(Long skuId);
}
