package com.itdr.services;

import com.itdr.common.ServerResponse;

public interface CategoryService {

    //根据分类ID查询所有的子类（包括本身）
    ServerResponse getDeepCategory(Integer categoryId);
}
