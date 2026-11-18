package com.stocklab.benchmark.exception;

/**
 * E2 乐观锁版本冲突：读到的库存 > 0，但 CAS 更新影响 0 行（version 已被其他事务改掉）。
 * 运行时异常，让 @Transactional 自动回滚，再由外层开启新事务重试。
 */
public class VersionConflictException extends RuntimeException {

    public VersionConflictException(Long skuId) {
        super("库存版本冲突，skuId=" + skuId);
    }
}
