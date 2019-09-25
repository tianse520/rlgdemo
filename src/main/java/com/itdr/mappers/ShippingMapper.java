package com.itdr.mappers;

import com.itdr.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    //
    Shipping selectByIdAndUid(@Param("shippingId") Integer shippingId, @Param("uid")Integer uid);
}