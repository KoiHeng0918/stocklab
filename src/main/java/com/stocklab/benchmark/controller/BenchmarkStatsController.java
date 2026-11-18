package com.stocklab.benchmark.controller;

import com.stocklab.benchmark.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 压测统计接口，不属于 4 个业务接口；正式 demo 可以关掉。
 */
@RestController
@RequestMapping("/benchmark")
public class BenchmarkStatsController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/retry-count")
    public long retryCount() {
        return orderService.getRetryCount();
    }
}
