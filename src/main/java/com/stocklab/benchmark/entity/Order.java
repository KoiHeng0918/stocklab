package com.stocklab.benchmark.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单实体，对应 orders 表。
 * request_id 上有唯一索引 uk_request_id，是幂等机制的地基。
 * 表名是 orders（复数），所以必须用 @TableName 显式指定。
 */
@Data
@TableName("orders")
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("request_id")
    private String requestId;

    private Long skuId;

    /** 0=CREATED，2=CANCELLED */
    private Integer status;

    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
