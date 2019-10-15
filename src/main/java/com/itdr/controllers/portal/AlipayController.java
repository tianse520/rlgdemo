package com.itdr.controllers.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.itdr.common.Const;
import com.itdr.common.ServerResponse;
import com.itdr.pojo.Users;
import com.itdr.pojo.pay.Configs;
import com.itdr.pojo.vo.CartVO;
import com.itdr.services.AliPayService;
import com.itdr.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Controller
@ResponseBody
@RequestMapping("/portal/order/")
public class AlipayController {

    //注入支付业务层
    @Autowired
    AliPayService aliPayService;

    /*订单支付*/
    @RequestMapping("pay.do")
    private ServerResponse alipay(Long orderNo, HttpSession session) {
        //用户是否登录
        Users users = (Users) session.getAttribute(Const.LOGINUSER);
        if (users == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(), Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return aliPayService.alipay(orderNo, users.getId());
    }

    /*查询订单支付状态*/
    @RequestMapping("query_order_pay_status.do")
    private ServerResponse queryOrderPayStatus(Long orderNo, HttpSession session) {
        //用户是否登录
        Users users = (Users) session.getAttribute(Const.LOGINUSER);
        if (users == null) {
            return ServerResponse.defeatedRS(Const.UsersEnum.NO_LOGIN.getCode(), Const.UsersEnum.NO_LOGIN.getDesc());
        }
        return aliPayService.queryOrderPayStatus(orderNo, users.getId());
    }

    /*支付宝回调*/
    @RequestMapping("alipay_callback.do")
    private String alipayCallback(HttpServletRequest request) {

        System.out.println("请求进来了");
        /*获取支付宝返回的参数，返回一个map集合*/
        Map<String, String[]> parameterMap = request.getParameterMap();
        /*获取上面的集合的键的set集合*/
        Set<String> strings = parameterMap.keySet();
        /*使用迭代器遍历键集合获得值*/
        Iterator<String> iterator = strings.iterator();
        /*创建一个接受参数的集合*/
        Map<String, String> newMap = new HashMap<>();
        /*便利迭代器，重新组装参数*/
        while (iterator.hasNext()) {
            /*根据键获取parameterMap中的值*/
            String key = iterator.next();
            String[] strings1 = parameterMap.get(key);
            /*遍历值的数组，重新拼装数据*/
            StringBuffer ss = new StringBuffer("");
            for (int i = 0; i < strings1.length; i++) {
                ss = (i == strings1.length - 1) ? ss.append(strings1[i]) : ss.append(strings1[i] + ",");
            }
            /*把新的数据以键值对的方式放入一个新的集合中*/
            newMap.put(key, ss.toString());
        }
        /*取出不必要参数*/
        newMap.remove("sign_type");
        try {
            /*使用官方验签方法验证*/
            boolean b = AlipaySignature.rsaCheckV2(newMap, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            System.out.println(b);

            /*验签通过执行下一步*/
            if (!b) {
                return "{'msg':'验签失败'}";
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
            return "{'msg':'验签失败'}";
        }

        /*验签通过，去业务层执行业务*/
        ServerResponse sr = aliPayService.alipayCallback(newMap);

        /*业务层处理完之后，返回对应的状态信息，这个信息是直接返回给支付宝服务器的，所以必须严格要求准确*/
        if (sr.isSuccess()) {
            return "SUCCESS";
        } else {
            return "FAILED";
        }

    }

}