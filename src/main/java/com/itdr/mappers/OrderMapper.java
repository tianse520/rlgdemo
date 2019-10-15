package com.itdr.mappers;

import com.itdr.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    /*根据订单对象修改订单状态码和支付时间、更新时间*/
    int updateByPrimaryKey(Order record);

    /*按订单编号查找订单*/
    Order selectByOrderNo(Long orderNo);

    /*判断订单编号和用户ID数据*/
    int selectByOrderNoAndUid(@Param("orderNo") Long orderNo, @Param("uid") Integer uid);

    //根据用户ID获取用户所有订单
    List<Order> selectByUid(Integer uid);

    int updateByToStatus(Order order);
}