package com.stocklab.benchmark.controller;

import com.stocklab.benchmark.entity.Stock;
import com.stocklab.benchmark.exception.BusinessException;
import com.stocklab.benchmark.service.StockService;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 查询当前库存；压测后用于核对剩余库存（E0b 会看到负数）。 */
@RestController
@RequestMapping("/stock")
@Validated
public class StockController {


    @Autowired
    private StockService stockService;


    @GetMapping("/getStock/{skuId}")
    public Stock get(
            @PathVariable
            @Positive(message = "skuId 必须大于 0")
            Long skuId) {

        Stock stock = stockService.getStock(skuId);
        if (stock == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "库存不存在");
        }
        return stock;
    }
}
