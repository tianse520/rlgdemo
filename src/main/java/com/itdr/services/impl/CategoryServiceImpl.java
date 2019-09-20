package com.itdr.services.impl;

import com.itdr.common.ServerResponse;
import com.itdr.mappers.CategoryMapper;
import com.itdr.pojo.Category;
import com.itdr.services.CategoryService;
import com.sun.org.apache.xml.internal.utils.ListingErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    CategoryMapper categoryMapper;
    //根据分类ID查询所有的子类（包括本身）
    @Override
    public ServerResponse getDeepCategory(Integer categoryId) {
        if (categoryId == null||categoryId<0){
            return ServerResponse.defeatedRS("非法的参数");
        }

        List<Integer> li = new ArrayList<>();
        li.add(categoryId);
        getAll(categoryId,li);

        ServerResponse rs = ServerResponse.successRS(li);
        return rs;
    }
    private void getAll(Integer pid, List<Integer> list){
        List<Category> li = categoryMapper.selectByParentId(pid);

        if (li != null&&li.size()!=0){
            for(Category category :li){
                list.add(category.getId());
                getAll(category.getId(),list);
            }
        }
    }
}
