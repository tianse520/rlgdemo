package com.itdr.utils;


import com.itdr.common.Const;
import com.itdr.pojo.Cart;
import com.itdr.pojo.Product;
import com.itdr.pojo.vo.CartProductVO;
import com.itdr.pojo.vo.ProductVo;

import java.io.IOException;
import java.math.BigDecimal;

public class PoToVoUtil {

    public static ProductVo ProductToProductVO(Product p) throws IOException {
        ProductVo pvo = new ProductVo();
        pvo.setImageHost(PropertiesUtil.getValue("imageHost"));
        pvo.setId(p.getId());
        pvo.setCategoryId(p.getCategoryId());
        pvo.setCreateTime(p.getCreateTime());
        pvo.setDetail(p.getDetail());
        pvo.setIsBanner(p.getIsBanner());
        pvo.setIsHot(p.getIsHot());
        pvo.setIsNew(p.getIsNew());
        pvo.setMainImage(p.getMainImage());
        pvo.setName(p.getName());
        pvo.setPrice(p.getPrice());
        pvo.setStatus(p.getStatus());
        pvo.setStock(p.getStock());
        pvo.setSubImages(p.getSubImages());
        pvo.setSubtitle(p.getSubtitle());
        pvo.setUpdateTime(p.getUpdateTime());

        return pvo;
    }

    public static CartProductVO getOne(Cart cart,Product p){
        //封装cartProductVo
        CartProductVO cartProductVO = new CartProductVO();
        cartProductVO.setId(cart.getId());
        cartProductVO.setUserId(cart.getUserId());
        cartProductVO.setProductId(cart.getProductId());
        cartProductVO.setQuantity(cart.getQuantity());
        cartProductVO.setProductChecked(cart.getChecked());

        //查询到的商品不能为NULL
        if (p!=null){
            cartProductVO.setName(p.getName());
            cartProductVO.setSubtitle(p.getSubtitle());
            cartProductVO.setMainImage(p.getMainImage());
            cartProductVO.setPrice(p.getPrice());
            cartProductVO.setStock(p.getStock());
            cartProductVO.setStatus(p.getStatus());
        }

        Integer count = 0;
        //处理库存问题
        if (cart.getQuantity()<=p.getStock()){
            count = cart.getQuantity();
            cartProductVO.setLimitQuantity(Const.Cart.LIMITQUANTITYSUCCESS);
        }else {
            count = p.getStock();
            cartProductVO.setLimitQuantity(Const.Cart.LIMITQUANTITYFAILED);
        }
        cartProductVO.setQuantity(count);

        //计算本条购物信息总价
        //注意先做库存处理，再做价格计算
        BigDecimal productTotalPrice = BigDecimalUtils.mul(p.getPrice().doubleValue(),cart.getQuantity());
        cartProductVO.setProductTotalPrice(productTotalPrice);

        return cartProductVO;
    }
}
