package com.stocklab.benchmark.exception;

import org.springframework.http.HttpStatus;

/**
 * 业务失败：库存不足、订单不存在等。
 * 带上 HTTP 状态，由全局异常处理器转成非 5xx 响应。
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
