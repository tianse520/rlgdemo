package com.itdr.services.impl;

import com.alipay.api.domain.Keyword;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itdr.common.ServerResponse;
import com.itdr.mappers.CategoryMapper;
import com.itdr.mappers.ProductMapper;
import com.itdr.pojo.Category;
import com.itdr.pojo.vo.ProductVo;
import com.itdr.services.ProductService;
import com.itdr.pojo.Product;
import com.itdr.utils.PoToVoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductMapper productMapper;
    @Autowired
    CategoryMapper categoryMapper;

    //获取商品分类信息
    @Override
    public ServerResponse<Product> topCategory(Integer pid) {
        if (pid == null || pid < 0) {
            return ServerResponse.defeatedRS("非法的参数");
        }

        //根据商品分类ID查询子分类
        List<Category> li = categoryMapper.selectByParentId(pid);

        if (li == null) {
            return ServerResponse.defeatedRS("查询的ID不存在");
        }
        if (li.size() == 0) {
            return ServerResponse.defeatedRS("没有子分类");
        }

        return ServerResponse.successRS(li);
    }

    //ctrl+O是实现接口方法的快捷键
    //获取商品详情
    @Override
    public ServerResponse<Product> detail(Integer productId, Integer is_new, Integer is_hot, Integer is_banner) {
        if (productId == null || productId < 0) {
            return ServerResponse.defeatedRS("非法的参数");
        }

        Product p = productMapper.selectByID(productId, is_new, is_hot, is_banner);
        if (p == null) {
            return ServerResponse.defeatedRS("商品不存在");
        }

        ProductVo productVO = null;
        try {
            productVO = PoToVoUtil.ProductToProductVO(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ServerResponse.successRS(productVO);
    }

    //获取最新、最热、banner商品详情
    @Override
    public ServerResponse<Product> detailNewOrHotOrBanner(Integer is_new, Integer is_hot, Integer is_banner) {
        List<Product> li = productMapper.selectByNewOrHotOrBanner(is_new, is_hot, is_banner);
        List<ProductVo> voList = new ArrayList<>();
        for (Product product : li) {
            try {
                ProductVo productVO = PoToVoUtil.ProductToProductVO(product);
                voList.add(productVO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ServerResponse.successRS(voList);
    }

    //商品搜索+动态排序，页码pageNum（要几页数据），条数pageSize（每一页几条数据）
    @Override
    public ServerResponse<Product> listProduct(Integer productId, String keyword, Integer pageNum, Integer pageSize, String orderBy) {
        if ((productId == null || productId < 0) && (keyword == null || keyword.equals(""))) {
            return ServerResponse.defeatedRS("非法参数");
        }

        //分割排序参数
        String[] split = new String[2];
        if (!orderBy.equals("")) {
            split = orderBy.split("_");
        }

        String keys = "%" + keyword + "%";
        /*使用PageHelper传入页码和每一页的数据，在sql语句上添加limit，能够顺利使用order by SQL语句*/
        PageHelper.startPage(pageNum, pageSize, split[0] + "" + split[1]);
        List<Product> li = productMapper.selectByIdOrName(productId, keys, split[0], split[1]);
        PageInfo pf = new PageInfo(li);
        return ServerResponse.successRS(pf);
    }
}
