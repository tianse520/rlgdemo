package com.itdr.mappers;

import com.itdr.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {

    //根据商品ID获取商品详情
    Product selectByID(@Param("productId") Integer productId,
                       @Param("is_new")Integer is_new,
                       @Param("is_hot")Integer is_hot,
                       @Param("is_banner")Integer is_banner);

    //根据商品ID或者商品名称关键字查询数据
    List<Product> selectByIdOrName(@Param("productId")Integer productId,
                                   @Param("keyword")String keyword,
                                   @Param("col")String col,
                                   @Param("order")String order);

    //根据商品ID获取商品数据
    Product selectByProductId(Integer productId);

    //根据商品ID更新商品数据
    int updateById(Product product);
}