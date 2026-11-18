package com.stocklab.benchmark.strategy;

import com.stocklab.benchmark.mapper.StockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * E1：条件更新。
 *
 * <p>不需要先 SELECT，也不需要在 Java 里判断库存；
 * 直接把“库存 >= 1”和“减 1”交给同一条 UPDATE。
 */
@Component
@ConditionalOnProperty(name = "benchmark.strategy", havingValue = "e1")
public class E1ConditionalUpdateStrategy implements StockDeductStrategy {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public boolean deduct(Long skuId) {
        int i  = stockMapper.e1DeductConditional(skuId);
        if(i <= 0){
            return false;
        }
        return true;
    }
}
