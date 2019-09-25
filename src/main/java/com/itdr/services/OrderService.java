package com.itdr.services;

import com.itdr.common.ServerResponse;

public interface OrderService {

    /*创建订单*/
    ServerResponse createOrder(Integer uid, Integer shippingId);
}
