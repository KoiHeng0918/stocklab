package com.stocklab.benchmark.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.stocklab.benchmark.entity.Order;
import com.stocklab.benchmark.entity.Stock;
import com.stocklab.benchmark.exception.BusinessException;
import com.stocklab.benchmark.exception.VersionConflictException;
import com.stocklab.benchmark.mapper.OrderMapper;
import com.stocklab.benchmark.mapper.StockMapper;
import com.stocklab.benchmark.service.OrderCreateTransactionService;
import com.stocklab.benchmark.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;


/**
 * 下单主流程：扣库存委托给 StockDeductStrategy，成功后再写订单。
 * 取消在 cancel 中处理。
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private OrderCreateTransactionService orderCreateTransactionService;

    private static final int MAX_RETRY = 3;

    private final AtomicLong retryCount = new AtomicLong();

    @Override
    public Order create(String requestId, Long skuId) {
        Order existing = getByRequestId(requestId);
        if (existing != null) {
            return existing;
        }
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                return orderCreateTransactionService.createInTransaction(requestId, skuId);
            } catch (DuplicateKeyException e) {
                Order winner = getByRequestId(requestId);
                if (winner != null) {
                    return winner;
                }
                throw e;
            } catch (VersionConflictException e) {
                // 版本冲突：事务已经回滚；只有还会再试时才计一次重试
                if (attempt < MAX_RETRY) {
                    retryCount.incrementAndGet();
                }
            }
        }

        // 3 次尝试都冲突（重试耗尽）：作为业务失败返回 409
        throw new BusinessException(HttpStatus.CONFLICT, "版本冲突，重试次数过多");
    }

    @Override
    public Order getByRequestId(String requestId) {
        return orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getRequestId, requestId)
        );
    }

    @Override
    public long getRetryCount() {
        return retryCount.get();
    }

    @Override
    @Transactional
    public Order cancel(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != null && order.getStatus() == 2) {
            return order; // 已经取消，直接返回
        }
        // 条件更新：只有 CREATED(0) 能改成 CANCELLED(2)
        int updated = orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getStatus, 0)
                        .set(Order::getStatus, 2));
        if (updated == 0) {
            // 并发取消：别人已经改了状态，不回库存
            order.setStatus(2);
            return order;
        }
        // 库存 +1，让数据库基于当前值加，不要先读再写
        int restored = stockMapper.update(null,
                new LambdaUpdateWrapper<Stock>()
                        .eq(Stock::getSkuId, order.getSkuId())
                        .setSql("stock = stock + 1"));
        if (restored == 0) {
            throw new IllegalStateException("库存回滚失败，skuId=" + order.getSkuId());
        }
        order.setStatus(2);
        return order;
    }
}
