package com.itdr.services.impl;

import com.itdr.common.Const;
import com.itdr.common.ServerResponse;
import com.itdr.mappers.CartMapper;
import com.itdr.mappers.ProductMapper;
import com.itdr.pojo.Cart;
import com.itdr.pojo.Product;
import com.itdr.pojo.vo.CartProductVO;
import com.itdr.pojo.vo.CartVO;
import com.itdr.services.CartService;
import com.itdr.utils.BigDecimalUtils;
import com.itdr.utils.PoToVoUtil;
import com.itdr.utils.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.awt.print.PrinterGraphics;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartMapper cartMapper;

    @Autowired
    ProductMapper productMapper;

    //购物车添加商品
    @Override
    public ServerResponse<CartVO> addOne(Integer productId, Integer count, Integer uid) {
        //参数非空判断
        if (productId == null || productId <= 0 || count == null || count <= 0) {
            return ServerResponse.defeatedRS(Const.CartEnum.EMPTY_NUM.getCode(),Const.CartEnum.EMPTY_NUM.getDesc());
        }

        //向购物车表中存储数据

        //如果有这条购物信息，就是更新购物数量，如果没有才是插入新数据
        Cart c2 = cartMapper.selectByUidAndProductID(uid, productId);
        if (c2 !=null) {
            //更新数据
            c2.setQuantity(c2.getQuantity()+count);
            int i = cartMapper.updateByPrimaryKeySelective(c2);
        } else {
            //插入新数据
            //创建一个Cart对象
            Cart c = new Cart();
            c.setUserId(uid);
            c.setProductId(productId);
            c.setQuantity(count);

            int insert = cartMapper.insert(c);
        }

//        CartVO cartVO = getCartVo(uid);
//        return ServerResponse.successRS(cartVO);
        return listCart(uid);
    }

    //购物车高可复用方法
    private CartVO getCartVo(Integer uid){
        //创建CartVO对象
        CartVO cartVO = new CartVO();

        //创建变量存储购物车总价
        BigDecimal cartTotalPrice = new BigDecimal("0");

        //用来存放CartProductVO对象的集合
        List<CartProductVO> cartProductVOList = new ArrayList<CartProductVO>();

        //根据用户ID查询该用户的所有购物车信息
        List<Cart> liCart = cartMapper.selectByUid(uid);

        //从购物信息集合中拿出每一条数据，根据其中的商品ID查询需要的商品信息
        if(liCart.size()!=0){
            for (Cart cart : liCart) {
                //根据购物信息中的商品ID查询商品数据
                Product p = productMapper.selectByID(cart.getProductId(),0,0,0);
                //使用工具类进行数据封装
                CartProductVO cartProductVO = PoToVoUtil.getOne(cart, p);

                //购物车更新有效库存,数据库里的数据和页面显示就会一样
                Cart cartForQuantity = new Cart();
                cartForQuantity.setId(cart.getId());
                cartForQuantity.setQuantity(cartProductVO.getQuantity());
                cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                //计算购物车总价
                //判断拿出的每一条信息是否被选中，计算选中的商品价格
                if (cart.getChecked() == Const.Cart.CHECK) {
                    cartTotalPrice = BigDecimalUtils.add(cartTotalPrice.doubleValue(), cartProductVO.getProductTotalPrice().doubleValue());
                }
                //把对象放到集合中
                cartProductVOList.add(cartProductVO);
            }
        }

        //封装cartVO数据
        cartVO.setCartProductVOList(cartProductVOList);
        cartVO.setAllChecked(this.checkALL(uid));
        cartVO.setCartTotalPrice(cartTotalPrice);
        try {
            cartVO.setImageHost(PropertiesUtil.getValue("imageHost"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cartVO;
    }
    //判断用户购物车是否全选
    private boolean checkALL(Integer uid){
        int i = cartMapper.selectByUidCheck(uid,Const.Cart.UNCHECK);
        if(i == 0){
            return true;
        }else{
            return false;
        }
    }

    //获取登陆用户的购物车列表
    @Override
    public ServerResponse<CartVO> listCart(Integer id) {
        CartVO cartVo = this.getCartVo(id);
        return ServerResponse.successRS(cartVo);
    }

    //购物车更新商品
    @Override
    public ServerResponse<CartVO> updateCart(Integer productId, Integer count, Integer id) {
        //参数非空判断
        if (productId == null || productId <= 0 || count == null || count <= 0) {
            return ServerResponse.defeatedRS(Const.CartEnum.EMPTY_NUM.getCode(),Const.CartEnum.EMPTY_NUM.getDesc());
        }

        //如果有这条购物信息，就是更新购物数量，如果没有才是插入新数据
        Cart c2 = cartMapper.selectByUidAndProductID(id, productId);

        //更新数据
        c2.setQuantity(count);
        int i = cartMapper.updateByPrimaryKeySelective(c2);

        return listCart(id);
    }

    //购物车删除商品
    @Override
    public ServerResponse<CartVO> deleteCart(String productIds, Integer id) {
        if (productIds == null||productIds.equals("")){
            return ServerResponse.defeatedRS(Const.CartEnum.EMPTY_NUM.getCode(),Const.CartEnum.EMPTY_NUM.getDesc());
        }

        //把字符串中的数据放到集合中
        String[] split = productIds.split(",");
        List<String> strings = Arrays.asList(split);

        int i = cartMapper.deleteByProduct(strings,id);
        return listCart(id);
    }

    //查询在购物车里的商品信息条数
    @Override
    public ServerResponse<Integer> getCartProductCount(Integer id) {
        List<Cart> carts = cartMapper.selectByUid(id);
        return ServerResponse.successRS(carts.size());
    }

    //改变购物车种商品选中状态
    @Override
    public ServerResponse<CartVO> selectOrUnSelect(Integer id,Integer check,Integer productId) {
        int i = cartMapper.selectOrUnSelect(id,check,productId);
        return listCart(id);
    }
}
