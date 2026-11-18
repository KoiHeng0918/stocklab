package com.stocklab.benchmark.strategy;

import com.stocklab.benchmark.entity.Stock;
import com.stocklab.benchmark.mapper.StockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * E0：先查后改的丢失更新。
 *
 * <p>两个事务都可能读到同一个库存值，都写回“读到的值 - 1”；
 * 后写覆盖先写，订单却各生成一张。
 */
@Component
@ConditionalOnProperty(name = "benchmark.strategy", havingValue = "e0", matchIfMissing = true)
public class E0LostUpdateStrategy implements StockDeductStrategy {

    @Autowired
    private StockMapper stockMapper;


    @Override
    public boolean deduct(Long skuId) {

        Stock stock = stockMapper.selectById(skuId);


        if (stock == null || stock.getStock() == null || stock.getStock() <= 0) {
            return false;
        }

        return stockMapper.e0UpdateStockValue(skuId, stock.getStock() - 1) == 1;

    }
}
