package com.stocklab.benchmark.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存实体，对应 stock 表（全项目只有 sku_id=1 这一行）。
 *
 * @TableName  指定表名；不加时 MyBatis-Plus 会按类名猜表名，显式写更清楚
 * @TableId    指定主键。sku_id 是手工指定的 1，不是自增，所以用 INPUT
 * @TableField 指定列名；updated_at 靠驼峰转换也能映射，这里显式写是示范注解用法
 */
@Data
@TableName("stock")
public class Stock {

    @TableId(value = "sku_id", type = IdType.INPUT)
    private Long skuId;

    private Integer total;

    private Integer stock;

    /** E2 乐观锁使用；每次 CAS 成功后 version + 1，初始值为 0 */
    private Integer version;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
