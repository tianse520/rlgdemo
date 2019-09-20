package com.itdr.services;

import com.itdr.common.ServerResponse;
import com.itdr.pojo.Product;

public interface ProductService {
    //获取商品分类信息
    ServerResponse<Product> topCategory(Integer pid);

    //获取商品详情
    ServerResponse<Product> detail(Integer productId, Integer is_new, Integer is_hot, Integer is_banner);

    //商品搜索+动态排序
    ServerResponse<Product> listProduct(Integer productId, String keyword, Integer pageNum, Integer pageSize, String orderBy);
}
