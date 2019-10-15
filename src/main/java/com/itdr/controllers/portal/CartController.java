package com.itdr.controllers.portal;

import com.itdr.common.Const;
import com.itdr.common.ServerResponse;
import com.itdr.pojo.Product;
import com.itdr.pojo.Users;
import com.itdr.pojo.vo.CartVO;
import com.itdr.services.CartService;
import com.itdr.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@ResponseBody
@RequestMapping("/portal/cart/")
public class CartController {

    @Autowired
    CartService cartService;

    //购物车添加商品
    @RequestMapping("add.do")
    public ServerResponse<CartVO> addOne(Integer productId, Integer count, HttpSession session) {
        Users users = (Users) session.getAttribute(Const.LOGINUSER);
        if (users == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(), Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return cartService.addOne(productId, count, users.getId());
    }

    //获取登陆用户的购物车列表
    @RequestMapping("list.do")
    public ServerResponse<CartVO> listCart(HttpSession session) {
        Users users = (Users) session.getAttribute(Const.LOGINUSER);
        if (users == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(), Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return cartService.listCart(users.getId());
    }

    //购物车更新商品
    @RequestMapping("update.do")
    public ServerResponse<CartVO> updateCart(Integer productId, Integer count, HttpSession session) {
        Users users = (Users) session.getAttribute(Const.LOGINUSER);
        if (users == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(), Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return cartService.updateCart(productId, count, users.getId());
    }

    //购物车删除商品
    @RequestMapping("delete_product.do")
    public ServerResponse<CartVO> deleteCart(String productIds, HttpSession session) {
        Users users = (Users) session.getAttribute(Const.LOGINUSER);
        if (users == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(), Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return cartService.deleteCart(productIds, users.getId());
    }

    //查询在购物车里的商品信息条数
    @RequestMapping("get_cart_product_count.do")
    public ServerResponse<Integer> getCartProductCount(HttpSession session) {
        Users users = (Users) session.getAttribute(Const.LOGINUSER);
        if (users == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(), Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return cartService.getCartProductCount(users.getId());
    }

    //购物车全选
    @RequestMapping("select_all.do")
    public ServerResponse<CartVO> selectAll(HttpSession session, Integer check) {
        Users users = (Users) session.getAttribute(Const.LOGINUSER);
        if (users == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(), Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return cartService.selectOrUnSelect(users.getId(), check, null);
    }

    //购物车取消全选
    @RequestMapping("un_select_all.do")
    public ServerResponse<CartVO> UnSelectAll(HttpSession session, Integer check) {
        Users users = (Users) session.getAttribute(Const.LOGINUSER);
        if (users == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(), Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return cartService.selectOrUnSelect(users.getId(), check, null);
    }

    //购物车选中某个商品
    @RequestMapping("select.do")
    public ServerResponse<CartVO> select(HttpSession session, Integer check, Integer productId) {
        Users users = (Users) session.getAttribute(Const.LOGINUSER);
        if (users == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(), Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return cartService.selectOrUnSelect(users.getId(), check, productId);
    }

    //购物车取消选中某个商品
    @RequestMapping("un_select.do")
    public ServerResponse<CartVO> unSelect(HttpSession session, Integer check, Integer productId) {
        Users users = (Users) session.getAttribute(Const.LOGINUSER);
        if (users == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(), Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return cartService.selectOrUnSelect(users.getId(), check, productId);
    }
}
