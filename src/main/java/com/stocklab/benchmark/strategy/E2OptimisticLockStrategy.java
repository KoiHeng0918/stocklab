package com.stocklab.benchmark.strategy;

import com.stocklab.benchmark.entity.Stock;
import com.stocklab.benchmark.exception.VersionConflictException;
import com.stocklab.benchmark.mapper.StockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * E2：乐观锁 CAS（单次尝试；重试由 OrderServiceImpl 在外层开启新事务完成）。
 *
 * <p>先读 stock/version，再用 version 做 CAS 更新；
 * 影响 0 行时，要区分“库存不足”和“版本冲突”。
 */
@Component
@ConditionalOnProperty(name = "benchmark.strategy", havingValue = "e2")
public class E2OptimisticLockStrategy implements StockDeductStrategy {



    @Autowired
    private StockMapper stockMapper;

    @Override
    public boolean deduct(Long skuId) {

        // 1. selectById(skuId) 读 stock/version
        Stock stock = stockMapper.selectById(skuId);

        // 2. stock 为空或 stock <= 0 → return false
        if (stock == null || stock.getStock() == null || stock.getStock() <= 0) {
            return false;
        }

        Integer version = stock.getVersion();

        if (version == null) {
            return false;
        }

        int affected = stockMapper.e2UpdateWithVersion(skuId,version);

        if(affected == 1) {
            return true;
        }


        throw new VersionConflictException(skuId);
    }
}
