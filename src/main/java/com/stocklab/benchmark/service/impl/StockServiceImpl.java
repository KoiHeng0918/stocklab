package com.stocklab.benchmark.service.impl;

import com.stocklab.benchmark.entity.Stock;
import com.stocklab.benchmark.mapper.StockMapper;
import com.stocklab.benchmark.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 库存查询服务：按 skuId 查 stock 表。 */
@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public Stock getStock(Long skuId) {
        return stockMapper.selectById(skuId);
    }


}
