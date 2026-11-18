-- ============================================================
-- stock-benchmark 建库建表脚本（MySQL 5.7）
-- 执行方式：Navicat 新建查询，整段执行一次即可
-- ============================================================

CREATE DATABASE IF NOT EXISTS stocklab
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE stocklab;

-- ------------------------------------------------------------
-- 库存表：故意只做单 SKU，业务极简，主线全部围绕它
-- version 字段是 E2 乐观锁实验才用上的，先建好不删列
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `stock` (
    `sku_id`     BIGINT      NOT NULL                COMMENT 'SKU 编号，固定用 1',
    `total`      INT         NOT NULL                COMMENT '库存总量',
    `stock`      INT         NOT NULL                COMMENT '剩余库存',
    `version`    INT         NOT NULL DEFAULT 0      COMMENT '乐观锁版本号（E2 实验用）',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`sku_id`)
) ENGINE = InnoDB COMMENT = '库存表（单 SKU）';

-- ------------------------------------------------------------
-- 订单表
-- uk_request_id 是幂等的根基：同一请求重复提交，第二次插入必然撞唯一键
-- idx_sku_status 供查单/对账使用
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `orders` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '订单号',
    `request_id` VARCHAR(64) NOT NULL                COMMENT '幂等键：客户端生成的请求唯一标识',
    `sku_id`     BIGINT      NOT NULL                COMMENT 'SKU 编号',
    `status`     TINYINT     NOT NULL DEFAULT 0      COMMENT '状态：0=CREATED 2=CANCELLED',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_request_id` (`request_id`),
    KEY `idx_sku_status` (`sku_id`, `status`)
) ENGINE = InnoDB COMMENT = '订单表';

-- ------------------------------------------------------------
-- 初始化库存：压测库存设 1000，小于请求总数才能测出超卖
-- ------------------------------------------------------------
INSERT INTO `stock` (`sku_id`, `total`, `stock`)
VALUES (1, 1000, 1000);

-- ------------------------------------------------------------
-- 压测重置：每组实验开跑前执行这两句，保证各组条件一致
-- ------------------------------------------------------------
-- UPDATE `stock` SET `stock` = `total` WHERE `sku_id` = 1;
-- TRUNCATE TABLE `orders`;
