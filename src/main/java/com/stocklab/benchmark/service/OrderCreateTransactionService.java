package com.stocklab.benchmark.service;

import com.stocklab.benchmark.entity.Order;
import com.stocklab.benchmark.exception.BusinessException;
import com.stocklab.benchmark.mapper.OrderMapper;
import com.stocklab.benchmark.strategy.StockDeductStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 下单的写事务边界：扣库存 + 写订单。
 *
 * <p>为什么要单独一个 Bean：
 * OrderServiceImpl.create 需要捕获 DuplicateKeyException，并在事务回滚后回查赢家订单。
 * 如果 catch 和 @Transactional 写在同一个方法里，事务不会自动回滚，输家扣的库存会被提交。
 *
 * <p>所以这里只负责“写”，异常直接往外抛，让 OrderServiceImpl 在事务外处理。
 */
@Service
public class OrderCreateTransactionService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StockDeductStrategy stockDeductStrategy;

    /**
     * 扣库存 + 写订单，必须在同一个事务里。
     * 库存不足抛 409；重复 requestId 撞唯一键时让异常抛出，由外层回查赢家。
     */
    @Transactional
    public Order createInTransaction(String requestId, Long skuId) {

        if (!stockDeductStrategy.deduct(skuId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "库存不足");
        }

        Order order = new Order();
        order.setRequestId(requestId);
        order.setSkuId(skuId);
        order.setStatus(0);
        orderMapper.insert(order);
        return order;
    }
}
