package com.itdr.controllers.portal;

import com.itdr.common.Const;
import com.itdr.common.ServerResponse;
import com.itdr.pojo.Users;
import com.itdr.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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


}