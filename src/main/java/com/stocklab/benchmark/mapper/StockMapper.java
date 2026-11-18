package com.stocklab.benchmark.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stocklab.benchmark.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 继承 BaseMapper 后，selectById / updateById 等单表 CRUD 不用自己写。
 * 各实验组的扣减 SQL 都加在这个接口里，方法名前缀表示实验组：
 * e0b = 单条 UPDATE 无下限；e0 = 先查后改丢失更新；
 * e1 = 条件更新；e2 = 版本 CAS；e3 = 悲观锁。
 */
@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    /**
     * E0b：无下限保护的扣减，用来和 E0 的丢失更新做对照。
     *
     * <p>这条 SQL 让数据库在当前库存上直接减 1：
     * 并发时两次减 1 都会生效，不会互相覆盖；
     * 但没有“库存必须大于 0”的判断，库存能被减成负数。
     *
     * <p>所以 E0b 的现象是“库存变负”；
     * E0 的现象是“订单数超过库存，库存不一定变负”。
     * 两者是不同缺陷，不能混着讲。
     *
     * <p>E1 会用 {@code UPDATE ... WHERE stock >= 1} 修掉这个缺陷。
     */
    @Update("UPDATE stock SET stock = stock - 1 WHERE sku_id = #{skuId} ")
    int e0bDeductWithoutLowerBound(@Param("skuId") Long skuId);

    /**
     * E0：把应用层读到的库存值原样写回。
     *
     * 并发时两个事务都读到 1000，都算出 999 并写回，
     * 后写覆盖先写，订单却会生成两张，这就是丢失更新。
     */
    @Update("UPDATE stock SET stock = #{stock} WHERE sku_id = #{skuId}")
    int e0UpdateStockValue(@Param("skuId") Long skuId, @Param("stock") Integer stock);

    /**
     * E1：条件更新。判断和扣减在同一条 SQL，InnoDB 行锁保证不会超卖。
     * 影响 1 行 = 成功；影响 0 行 = 库存不足。
     */
    @Update("UPDATE stock SET stock = stock - 1 WHERE sku_id = #{skuId} AND stock >= 1")
    int e1DeductConditional(@Param("skuId") Long skuId);

    /**
     * E2：乐观锁 CAS。
     * version 对不上，或者库存不足，都会影响 0 行。
     */
    @Update("UPDATE stock SET stock = stock - 1, version = version + 1 " +
            "WHERE sku_id = #{skuId} AND version = #{version} AND stock >= 1")
    int e2UpdateWithVersion(@Param("skuId") Long skuId, @Param("version") Integer version);

    /**
     * E3：悲观锁。SELECT ... FOR UPDATE 会锁住这一行，
     * 直到所在事务提交或回滚。
     */
    @Select("SELECT * FROM stock WHERE sku_id = #{skuId} FOR UPDATE")
    Stock selectByIdForUpdate(@Param("skuId") Long skuId);

    /**
     * E3：持锁后的扣减。库存已经在锁内判断过，这里直接减 1。
     */
    @Update("UPDATE stock SET stock = stock - 1 WHERE sku_id = #{skuId}")
    int e3Deduct(@Param("skuId") Long skuId);
}
