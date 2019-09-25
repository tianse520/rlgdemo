package com.itdr.mappers;

import com.itdr.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    //根据用户ID和商品ID判断数据是否存在
    Cart selectByUidAndProductID(@Param("uid") Integer uid, @Param("productId")Integer productId);

    //根据用户ID查询所有购物数据
    List<Cart> selectByUid(Integer uid);

    //根据用户ID判断用户购物车是否全选
    int selectByUidCheck(@Param("uid")Integer uid,@Param("check")Integer check);
    //购物车删除商品
    int deleteByProduct(@Param("productIds")List<String> productIds,@Param("id")Integer id);

    //改变购物车种商品选中状态
    int selectOrUnSelect(@Param("id")Integer id,@Param("check")Integer check,@Param("productId")Integer productId);

    /*获取用户购物车中选中的商品数据*/
    List<Cart> selectByUidAll(Integer uid);

    //根据商品ID和用户ID删除购物车数据
    int deleteAllByIdAndUid(@Param("li")List<Cart> li, @Param("uid")Integer uid);
}