package com.stocklab.benchmark.service;

import com.stocklab.benchmark.entity.Order;

/** 订单服务接口：下单主链路的业务契约都在这里声明。 */
public interface OrderService {

    Order create(String requestId, Long skuId);

    Order getByRequestId(String requestId);

    long getRetryCount();

    Order cancel(Long id);
}
