package com.itdr.services;

import com.itdr.common.ServerResponse;

import java.util.Map;

public interface AliPayService {
    /*订单支付*/
    ServerResponse alipay(Long orderNo, Integer uid);

    /*支付宝回调*/
    ServerResponse alipayCallback(Map<String, String> newMap);

    /*查询订单支付状态*/
    ServerResponse queryOrderPayStatus(Long orderNo, Integer id);
}
