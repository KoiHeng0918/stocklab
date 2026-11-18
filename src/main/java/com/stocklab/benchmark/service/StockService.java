package com.stocklab.benchmark.service;

import com.stocklab.benchmark.entity.Stock;

/** 库存服务接口：Controller 只依赖接口，不碰 Mapper。 */
public interface StockService {

    Stock getStock(Long skuId);


}
