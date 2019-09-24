package com.itdr.services.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.itdr.common.Const;
import com.itdr.common.ServerResponse;
import com.itdr.mappers.*;
import com.itdr.pojo.Order;
import com.itdr.pojo.OrderItem;
import com.itdr.pojo.PayInFo;
import com.itdr.pojo.pay.Configs;
import com.itdr.pojo.pay.ZxingUtils;
import com.itdr.services.AliPayService;
import com.itdr.utils.DateUtils;
import com.itdr.utils.JsonUtils;
import com.itdr.utils.PoToVoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInFoMapper payInFoMapper;

    /*订单支付*/
    @Override
    public ServerResponse alipay(Long orderNo, Integer uid) {
        //参数非空判断
        if (orderNo == null || orderNo <= 0) {
            return ServerResponse.defeatedRS(Const.CartEnum.EMPTY_NUM.getCode(),Const.CartEnum.EMPTY_NUM.getDesc());
        }
        //判断订单是否存在
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.defeatedRS(Const.PaymentPlatformEnum.EMPTY_ALIPAY.getCode(),Const.PaymentPlatformEnum.EMPTY_ALIPAY.getDesc());
        }
        //判断订单状态是否是未付款
        if (order.getStatus() !=10) {
            return ServerResponse.defeatedRS(Const.PaymentPlatformEnum.ERROR_CART.getCode(),Const.PaymentPlatformEnum.ERROR_CART.getDesc());
        }
        //判断订单和用户是否匹配
        int i = orderMapper.selectByOrderNoAndUid(orderNo, uid);
        if (i <= 0) {
            return ServerResponse.defeatedRS(Const.PaymentPlatformEnum.ERROR_USER_CART.getCode(),Const.PaymentPlatformEnum.ERROR_USER_CART.getDesc());
        }

        //根据订单号查询对应商品详情
        List<OrderItem> orderItems = orderItemMapper.selectByOrderNo(order.getOrderNo());
        //调用支付宝接口获取支付二维码
        try {
            //使用封装方法获得预下单成功后返回的二维码信息串
            AlipayTradePrecreateResponse response = test_trade_precreate(order, orderItems);

            //响应成功才执行下一步
            if (response.isSuccess()){
                //将二维码信息串生成图片，并保存，（需要修改为运行机器上的路径）
                String filePath = String.format(Configs.getSavecode_test()+"qr-%s.png",
                        response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);

                //预下单成功获取返回信息（订单编号+二维码字符串图片地址）
                Map map = new HashMap();
                map.put("orderNo", order.getOrderNo());

                map.put("qrCode", filePath);
                return ServerResponse.successRS(map);
            }else {
                return ServerResponse.defeatedRS("下单失败");
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return ServerResponse.defeatedRS("下单失败");
        }
        //后期图片会放到图片服务器上
    }

    /*查询订单支付状态*/
    @Override
    public ServerResponse queryOrderPayStatus(Long orderNo, Integer id) {
        /*订单状态：0-已取消 10-未付款 20-已付款 40-已发货 50-交易成功 60-交易关闭*/
        //判断订单是否存在
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.defeatedRS("该订单不存在");
        }
        if (order.getStatus() ==10) {
            return ServerResponse.defeatedRS("该订单未付款");
        }
        if (order.getStatus() ==20) {
            return ServerResponse.defeatedRS("该订单已付款");
        }
        if (order.getStatus() ==40) {
            return ServerResponse.defeatedRS("该订单已发货");
        }
        if (order.getStatus() ==50) {
            return ServerResponse.defeatedRS("该订单已交易成功");
        }
        //判断订单状态是否是未付款
        if (order.getStatus() ==60) {
            return ServerResponse.defeatedRS("该订单交易已关闭");
        }
        return ServerResponse.defeatedRS("订单状态错误");
    }

    //测试当面付2.0生成支付二维码
    private AlipayTradePrecreateResponse test_trade_precreate(Order order, List<OrderItem> orderItems) throws
            AlipayApiException {
        //读取配置文件信息
        Configs.init("zfbinfo.properties");

        //实例化支付宝客户端
        AlipayClient alipayClient = new DefaultAlipayClient(Configs.getOpenApiDomain(),
                Configs.getAppid(), Configs.getPrivateKey(), "json", "utf-8",
                Configs.getAlipayPublicKey(), Configs.getSignType());

        //创建API对应的request类
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

        //获取一个BizContent对象,并转换成json格式
        String str = JsonUtils.obj2String(PoToVoUtil.getBizContent(order, orderItems));
        request.setBizContent(str);
        //设置支付宝回调路径
        request.setNotifyUrl(Configs.getNotifyUrl_test());
        //获取响应,这里要处理一下异常
        AlipayTradePrecreateResponse response = alipayClient.execute(request);

        //返回响应的结果
        return response;
    }

    /*支付宝回调*/
    @Override
    public ServerResponse alipayCallback(Map<String, String> newMap) {

        //获取ordrNo,就是订单编号
        Long orderNo = Long.parseLong(newMap.get("out_trade_no"));
        //获取流水号
        String tarde_no = newMap.get("trade_no");
        //获取支付状态
        String trade_status = newMap.get("trade_status");
        //获取支付时间
        String payment_time = newMap.get("gmt_payment");
        //获取订单金额
        BigDecimal totalAmount =new BigDecimal(newMap.get("total_amount"));
        //获取订单中的用户ID
//        Integer uid =Integer.parseInt(newMap.get("uid"));

        /*验证订单是否存在*/
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            //不是要付款的订单
            return ServerResponse.defeatedRS("不是要付款的订单");
        }

        //验证订单金额和数据库中订单金额是否相同
        if (!totalAmount.equals(order.getPayment())){
            return ServerResponse.defeatedRS("订单金额不匹配");
        }

        //订单和用户是否匹配
//        int i = orderMapper.selectByOrderNoAndUid(orderNo, uid);
//        if (i<=0){
//            return ServerResponse.defeatedRS("订单和用户不匹配");
//        }
        //防止支付宝重复回调
        if (order.getStatus()!=10) {
            return ServerResponse.defeatedRS("订单不是未付款状态");
        }

        //交易状态判断
        if (trade_status.equals(Const.TRADE_SUCCESS)) {
            //校验状态码，支付成功
            //更改数据库中订单的状态+更改支付时间+更新时间+删除用过的本地二维码
            order.setStatus(20);
            order.setPaymentTime(DateUtils.strToDate(payment_time));
            orderMapper.updateByPrimaryKey(order);

            //支付成功，删除本地存在的二维码图片
            String str = String.format(Configs.getSavecode_test()+"qr-%s.png",
                    order.getOrderNo());
            File file = new File(str);
            boolean b = file.delete();

            //还可以判断图片是否删除成功

        }

        //保存支付宝支付信息(任何状态都保存)
        PayInFo payInfo = new PayInFo();
        payInfo.setOrderNo(orderNo);
        payInfo.setPayPlatform(Const.PaymentPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformStatus(trade_status);
        payInfo.setPlatformNumber(tarde_no);
        payInfo.setUserId(order.getUserId());

        //将支付信息插入到数据库表中
        int result = payInFoMapper.insert(payInfo);
        if (result <= 0) {
            //支付信息保存失败返回结果
            return ServerResponse.defeatedRS(Const.PaymentPlatformEnum.SAVEPAYMSG_FALSE.getCode(), Const.PaymentPlatformEnum.SAVEPAYMSG_FALSE.getDesc());
        }
        //支付信息保存成功返回结果SUCCESS，让支付宝不再回调
        return ServerResponse.successRS("SUCCESS");
    }
}
