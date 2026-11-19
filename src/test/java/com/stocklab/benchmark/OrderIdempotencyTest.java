package com.stocklab.benchmark;

import com.stocklab.benchmark.entity.Order;
import com.stocklab.benchmark.exception.BusinessException;
import com.stocklab.benchmark.service.OrderService;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "benchmark.strategy=e1")
class OrderIdempotencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void reset() {
        // 用 sku_id = 2 做测试，不碰 E0~E3 使用的 sku_id = 1
        jdbcTemplate.update("""
                INSERT INTO stock (sku_id, total, stock, version)
                VALUES (2, 1000, 1000, 0)
                ON DUPLICATE KEY UPDATE total = 1000, stock = 1000, version = 0
                """);
        jdbcTemplate.update("DELETE FROM orders WHERE request_id LIKE 'test-%'");
    }

    @Test
    void sameRequestIdTwice_onlyOneOrderAndOneDeduction() {
        Order first = orderService.create("test-idem-1", 2L);
        Order second = orderService.create("test-idem-1", 2L);

        assertEquals(first.getId(), second.getId());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE request_id = 'test-idem-1'",
                Integer.class);
        Integer stock = jdbcTemplate.queryForObject(
                "SELECT stock FROM stock WHERE sku_id = 2",
                Integer.class);

        assertEquals(1, count);
        assertEquals(999, stock);
    }

    @Test
    void concurrentSameRequestId_onlyOneOrderAndOneDeduction() throws Exception {
        int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        //ready 解决“主线程等所有线程准备好”
        //start 解决“所有线程等主线程统一发令”
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Order>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    return orderService.create("test-idem-concurrent", 2L);
                }));
            }

            // 等 16 个线程都准备好，再同时放行
            ready.await();
            start.countDown();

            Set<Long> orderIds = new HashSet<>();
            for (Future<Order> future : futures) {
                orderIds.add(future.get(5, TimeUnit.SECONDS).getId());
            }

            // 所有线程拿到的都是同一张订单
            assertEquals(1, orderIds.size());

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM orders WHERE request_id = 'test-idem-concurrent'",
                    Integer.class);
            Integer stock = jdbcTemplate.queryForObject(
                    "SELECT stock FROM stock WHERE sku_id = 2",
                    Integer.class);

            assertEquals(1, count);
            assertEquals(999, stock);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void cancelTwice_onlyOneStockRollback() {
        Order order = orderService.create("test-cancel-1", 2L);
        assertEquals(999, stockOfTestSku());

        Order firstCancel = orderService.cancel(order.getId());
        assertEquals(2, firstCancel.getStatus());
        assertEquals(1000, stockOfTestSku());

        Order secondCancel = orderService.cancel(order.getId());
        assertEquals(2, secondCancel.getStatus());
        assertEquals(1000, stockOfTestSku());

        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM orders WHERE id = ?",
                Integer.class,
                order.getId());
        assertEquals(2, status);
    }

    @Test
    void insufficientStock_throws409() {
        jdbcTemplate.update("UPDATE stock SET stock = 0 WHERE sku_id = 2");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.create("test-409-1", 2L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals(0, stockOfTestSku());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE request_id = 'test-409-1'",
                Integer.class);
        assertEquals(0, count);
    }

    @Test
    void concurrentCancel_onlyOneStockRollback() throws Exception {
        Order order = orderService.create("test-cancel-concurrent", 2L);
        assertEquals(999, stockOfTestSku());

        int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Order>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    return orderService.cancel(order.getId());
                }));
            }

            ready.await();
            start.countDown();

            for (Future<Order> future : futures) {
                assertEquals(2, future.get(5, TimeUnit.SECONDS).getStatus());
            }

            // 16 个线程同时取消，库存只能回滚一次
            assertEquals(1000, stockOfTestSku());

            Integer status = jdbcTemplate.queryForObject(
                    "SELECT status FROM orders WHERE id = ?",
                    Integer.class,
                    order.getId());
            assertEquals(2, status);
        } finally {
            pool.shutdownNow();
        }
    }

    private Integer stockOfTestSku() {
        return jdbcTemplate.queryForObject(
                "SELECT stock FROM stock WHERE sku_id = 2",
                Integer.class);
    }
}
