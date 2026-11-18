package com.stocklab.benchmark.strategy;

import com.stocklab.benchmark.mapper.StockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * E0b：单条 UPDATE，无下限保护。
 *
 * <p>故意不做库存判断：只要 sku_id 存在，就直接 stock = stock - 1。
 * 并发下每个扣减都会生效，不会丢失更新；
 * 但没有 stock >= 1 的保护，库存能被减成负数。
 */
@Component
@ConditionalOnProperty(name = "benchmark.strategy", havingValue = "e0b")
public class E0bNoLowerBoundStrategy implements StockDeductStrategy {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public boolean deduct(Long skuId) {
        return stockMapper.e0bDeductWithoutLowerBound(skuId) == 1;
    }
}
