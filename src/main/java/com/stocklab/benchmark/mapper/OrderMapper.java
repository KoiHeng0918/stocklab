package com.stocklab.benchmark.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stocklab.benchmark.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/** 订单仓储：目前只用 BaseMapper 自带方法，跨表逻辑放在 Service 层。 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
