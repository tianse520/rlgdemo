package com.itdr.mappers;

import com.itdr.pojo.Order;
import com.itdr.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    /*按订单编号查找订单*/
    List<OrderItem> selectByOrderNo(Long oid);

    //创建订单详情,没有问题要存到数据库中,这里要使用批量插入的方式
    int insertAll(@Param("orderItem") List<OrderItem> orderItem);

    //根据订单号和用户ID查对应商品详情
    List<OrderItem> selectByOrderNoAndUid(@Param("orderNo") Long orderNo, @Param("uid") Integer uid);
}