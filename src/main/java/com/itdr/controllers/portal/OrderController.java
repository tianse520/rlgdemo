package com.itdr.controllers.portal;

import com.itdr.common.Const;
import com.itdr.common.ServerResponse;
import com.itdr.pojo.Users;
import com.itdr.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@ResponseBody
@RequestMapping("/order/")
public class OrderController {

    @Autowired
    OrderService orderService;

    /*创建订单*/
    @RequestMapping("create.do")
    public ServerResponse createOrder(HttpSession session,Integer shippingId ){
        Users u = (Users)session.getAttribute(Const.LOGINUSER);
        if (u== null){
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(),Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return orderService.createOrder(u.getId(),shippingId);
    }

    /*获取订单详情信息*/
    @RequestMapping("get_orderItems.do")
    public ServerResponse getOrderItems(HttpSession session,
                                        @RequestParam(value = "orderNo",required = false) Long orderNo){
        Users u = (Users)session.getAttribute(Const.LOGINUSER);
        if (u== null){
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(),Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return orderService.getOrderItems(u.getId(),orderNo);
    }

    /*获取用户商品列表*/
    @RequestMapping("list_order.do")
    public ServerResponse getOrderList(HttpSession session,
                                        @RequestParam(value = "pageSize",required = false,defaultValue = "10") Integer pageSize,
                                       @RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum){
        Users u = (Users)session.getAttribute(Const.LOGINUSER);
        if (u== null){
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(),Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return orderService.getOrderList(u.getId(),pageSize,pageNum);
    }

    /*取消订单*/
    @RequestMapping("countermand_order.do")
    public ServerResponse countermandOrder(HttpSession session,Long orderNo){
        Users u = (Users)session.getAttribute(Const.LOGINUSER);
        if (u== null){
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(),Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return orderService.countermandOrder(u.getId(),orderNo);
    }
}