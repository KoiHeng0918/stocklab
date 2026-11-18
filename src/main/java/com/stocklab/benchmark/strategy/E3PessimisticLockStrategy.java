package com.stocklab.benchmark.strategy;

import com.stocklab.benchmark.entity.Stock;
import com.stocklab.benchmark.mapper.StockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * E3：悲观锁。
 *
 * <p>SELECT ... FOR UPDATE 锁住库存行，直到事务提交；
 * 其他事务想更新同一行会排队等待。
 */
@Component
@ConditionalOnProperty(name = "benchmark.strategy", havingValue = "e3")
public class E3PessimisticLockStrategy implements StockDeductStrategy {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public boolean deduct(Long skuId) {
        // 锁定读：锁一直持有到 createInTransaction 的事务提交/回滚
        Stock stock = stockMapper.selectByIdForUpdate(skuId);

        if (stock == null || stock.getStock() == null || stock.getStock() <= 0) {
            return false;
        }

        return stockMapper.e3Deduct(skuId) == 1;
    }
}
