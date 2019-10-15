package com.itdr.services.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itdr.common.ServerResponse;
import com.itdr.mappers.*;
import com.itdr.pojo.*;
import com.itdr.pojo.vo.OrderItemListVO;
import com.itdr.pojo.vo.OrderItemVO;
import com.itdr.pojo.vo.OrderVO;
import com.itdr.pojo.vo.ShippingVO;
import com.itdr.services.OrderService;
import com.itdr.utils.BigDecimalUtils;
import com.itdr.utils.DateUtils;
import com.itdr.utils.PoToVoUtil;
import com.itdr.utils.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartMapper cartMapper;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    ShippingMapper shippingMapper;

    /*创建订单*/
    @Override
    public ServerResponse createOrder(Integer uid, Integer shippingId) {
        /*参数判断*/
        if (shippingId == null || shippingId <= 0) {
            return ServerResponse.defeatedRS("非法参数");
        }
        //存储订单选中的商品数据
        List<Product> productList = new ArrayList<>();
        /*获取用户购物车中选中的商品数据*/
        List<Cart> li = cartMapper.selectByUidAll(uid);
        if (li.size() == 0) {
            return ServerResponse.defeatedRS("至少选中一件商品");
        }
        //获取用户地址信息
        Shipping shipping = shippingMapper.selectByIdAndUid(shippingId, uid);
        if (shipping == null) {
            return ServerResponse.defeatedRS("用户收货地址不存在");
        }
        //计算订单总价
        BigDecimal payment = new BigDecimal("0");
        for (Cart cart : li) {
            //判断商品是否失效
            Integer productId = cart.getProductId();
            //根据商品ID获取商品数据
            Product p = productMapper.selectByProductId(productId);
            if (p == null) {
                return ServerResponse.defeatedRS("商品不存在");
            }
            if (p.getStatus() != 1) {
                return ServerResponse.defeatedRS(p.getName() + "商品已下架");
            }
            //校验库存
            if (cart.getQuantity() > p.getStock()) {
                return ServerResponse.defeatedRS(p.getName() + "超出库存数量");
            }
            //根据购物车购物数量和商品单价，计算一条购物车信息的总价
            BigDecimal mul = BigDecimalUtils.mul(p.getPrice().doubleValue(), cart.getQuantity());
            //把每一条购物车信息总价相加，就是订单总价
            payment = BigDecimalUtils.add(payment.doubleValue(), mul.doubleValue());
            //放到集合中备用
            productList.add(p);
        }

        /*创建订单,没有问题要存到数据库中*/
        Order order = this.getOrder(uid, shippingId);
        int insert = orderMapper.insert(order);
        if (insert <= 0) {
            ServerResponse.defeatedRS(order.getOrderNo() + "订单创建失败");
        }
        /*创建订单详情,没有问题要存到数据库中,这里要使用批量插入的方式*/
        List<OrderItem> orderItemList = this.getOrderItem(uid, order.getOrderNo(), productList, li);
        int insertAll = orderItemMapper.insertAll(orderItemList);
        if (insertAll <= 0) {
            return ServerResponse.defeatedRS(order.getOrderNo() + "创建订单详情失败");
        }
        //插入成功，减少商品库存
        for (OrderItem item : orderItemList) {
            for (Product product : productList) {
                if (item.getProductId() == product.getId()) {
                    Integer count = product.getStock() - item.getQuantity();
                    if (count < 0) {
                        return ServerResponse.defeatedRS("库存不能为负数");
                    }
                    product.setStock(count);
                    //更新数据到数据库当中
                    int inProduct = productMapper.updateById(product);
                    if (inProduct < 0) {
                        return ServerResponse.defeatedRS("更新商品库存失败");
                    }
                }
            }
        }
        //清空购物车
        int cartDelete = cartMapper.deleteAllByIdAndUid(li, uid);
        if (cartDelete <= 0) {
            return ServerResponse.defeatedRS("清空购物车失败");
        }

        /*拼接VO类,返回数据*/
        List<OrderItemVO> itemVOList = this.getOrderItemVOList(orderItemList);
//        for (OrderItem orderItem : orderItemList) {
//            OrderItemVO orderItemVO = PoToVoUtil.orderItemToOrderItemVO(orderItem);
//            itemVOList.add(orderItemVO);
//        }
        //封装地址VO类
        ShippingVO shippingVO = PoToVoUtil.shippingToshippingVO(shipping);

        OrderVO orderVO = new OrderVO();
        orderVO.setOrderItemVOList(itemVOList);
        orderVO.setShippingVO(shippingVO);
        orderVO.setOrderNo(order.getOrderNo());
        orderVO.setShippingId(shippingId);
        orderVO.setPayment(order.getPayment());
        orderVO.setPaymentType(order.getPaymentType());
        orderVO.setPostage(order.getPostage());
        orderVO.setStatus(order.getStatus());
        orderVO.setPaymentTime(DateUtils.dateToStr(order.getPaymentTime()));
        System.out.println(order.getPaymentTime());
        System.out.println(order.getCreateTime());
        orderVO.setSendTime(DateUtils.dateToStr(order.getSendTime()));
        orderVO.setEndTime(DateUtils.dateToStr(order.getEndTime()));
        orderVO.setCloseTime(DateUtils.dateToStr(order.getCloseTime()));
        orderVO.setCreateTime(DateUtils.dateToStr(order.getCreateTime()));
        try {
            orderVO.setImageHost(PropertiesUtil.getValue("imageHost"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ServerResponse.successRS(orderVO);
    }

    /*获取订单详情信息*/
    @Override
    public ServerResponse getOrderItems(Integer uid, Long orderNo) {
        OrderItemListVO orderItemListVO = new OrderItemListVO();
        List<OrderItem> itemList = null;
        List<OrderItemVO> itemVOList = null;
        BigDecimal payment = new BigDecimal("0");

        //根据用户ID和订单编号获取对应的订单详情
        if (orderNo != null) {
            itemList = orderItemMapper.selectByOrderNoAndUid(orderNo, uid);
            itemVOList = this.getOrderItemVOList(itemList);
            Order order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                return ServerResponse.defeatedRS(orderNo + "订单不存在");
            }
            payment = order.getPayment();
        } else {
            //没有订单ID，根据用户ID获取订单详情信息
            /*获取用户购物车中选中的商品数据*/
            List<Cart> li = cartMapper.selectByUidAll(uid);
            if (li.size() == 0) {
                return ServerResponse.defeatedRS("至少选中一件商品");
            }

            //存储订单选中的商品数据
            List<Product> productList = new ArrayList<>();
            for (Cart cart : li) {
                //判断商品是否失效
                Integer productId = cart.getProductId();
                //根据商品ID获取商品数据
                Product p = productMapper.selectByProductId(productId);
                if (p == null) {
                    return ServerResponse.defeatedRS("商品不存在");
                }
                if (p.getStatus() != 1) {
                    return ServerResponse.defeatedRS(p.getName() + "商品已下架");
                }
                //校验库存
                if (cart.getQuantity() > p.getStock()) {
                    return ServerResponse.defeatedRS(p.getName() + "超出库存数量");
                }
                //根据购物车购物数量和商品单价，计算一条购物车信息的总价
                BigDecimal mul = BigDecimalUtils.mul(p.getPrice().doubleValue(), cart.getQuantity());
                //把每一条购物车信息总价相加，就是订单总价
                payment = BigDecimalUtils.add(payment.doubleValue(), mul.doubleValue());
                //放到集合中备用
                productList.add(p);
            }

            itemList = this.getOrderItem(uid, null, productList, li);
            itemVOList = this.getOrderItemVOList(itemList);
        }

        //拼装orderItemListVO
        orderItemListVO.setOrderItemVoList(itemVOList);
        try {
            orderItemListVO.setImageHost(PropertiesUtil.getValue("imageHost"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        orderItemListVO.setProductTotalPrice(payment);

        return ServerResponse.successRS(orderItemListVO);
    }

    //获取用户订单列表
    @Override
    public ServerResponse getOrderList(Integer uid, Integer pageSize, Integer pageNum) {
        OrderVO orderVO = new OrderVO();
        List<OrderVO> orderVOList = new ArrayList<>();

        //获取用户的所有订单
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUid(uid);

        //循环创建OrderVO对象
        for (Order order : orderList) {
            List<OrderItem> itemList = orderItemMapper.selectByOrderNoAndUid(order.getOrderNo(), uid);
            List<OrderItemVO> itemVOList = this.getOrderItemVOList(itemList);
            Shipping shipping = shippingMapper.selectByIdAndUid(order.getShippingId(), uid);

            ShippingVO shippingVO = PoToVoUtil.shippingToshippingVO(shipping);

            orderVO.setOrderItemVOList(itemVOList);
            orderVO.setShippingVO(shippingVO);
            orderVO.setOrderNo(order.getOrderNo());
            orderVO.setShippingId(order.getShippingId());
            orderVO.setPayment(order.getPayment());
            orderVO.setPaymentType(order.getPaymentType());
            orderVO.setPostage(order.getPostage());
            orderVO.setStatus(order.getStatus());
            orderVO.setPaymentTime(DateUtils.dateToStr(order.getPaymentTime()));
            System.out.println(order.getPaymentTime());
            System.out.println(order.getCreateTime());
            orderVO.setSendTime(DateUtils.dateToStr(order.getSendTime()));
            orderVO.setEndTime(DateUtils.dateToStr(order.getEndTime()));
            orderVO.setCloseTime(DateUtils.dateToStr(order.getCloseTime()));
            orderVO.setCreateTime(DateUtils.dateToStr(order.getCreateTime()));
            try {
                orderVO.setImageHost(PropertiesUtil.getValue("imageHost"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            orderVOList.add(orderVO);
        }

        PageInfo pageInfo = new PageInfo(orderVOList);
        pageInfo.setList(orderVOList);

        return ServerResponse.successRS(pageInfo);
    }

    /*取消订单*/
    @Override
    public ServerResponse countermandOrder(Integer uid, Long orderNo) {
        if (orderNo == null || orderNo <= 0) {
            return ServerResponse.defeatedRS("非法参数");
        }

        //判断订单是否存在
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.defeatedRS(orderNo + "订单不存在");
        }

        //判断订单是不是未付款的状态
        if (order.getStatus() != 10) {
            return ServerResponse.defeatedRS(orderNo + "不是未付款状态");
        }

        //取消订单，改变订单状态
        order.setStatus(0);
        int i = orderMapper.updateByToStatus(order);
        if (i <= 0) {
            return ServerResponse.defeatedRS(orderNo + "订单取消失败");
        }

        //取消库存锁定
        List<OrderItem> itemList = orderItemMapper.selectByOrderNo(orderNo);
        for (OrderItem orderItem : itemList) {
            Product product = productMapper.selectByProductId(orderItem.getProductId());
            product.setStock(product.getStock() + orderItem.getQuantity());
            int iProduct = productMapper.updateById(product);
            if (iProduct <= 0) {
                return ServerResponse.defeatedRS("商品更新失败");
            }
        }
        return ServerResponse.successRS("用户取消成功");
    }

    /*创建一个订单对象*/
    private Order getOrder(Integer uid, Integer shippingId) {
        Order o = new Order();
        o.setUserId(uid);
        o.setOrderNo(this.getOrderNo());
        o.setShippingId(shippingId);
        o.setPaymentType(1);
        o.setPostage(0);
        o.setStatus(10);
        return o;
    }

    /*根据订单详情集合获取OrderItemVO集合*/
    private List<OrderItemVO> getOrderItemVOList(List<OrderItem> orderItemList) {
        /*拼接VO类,返回数据*/
        List<OrderItemVO> itemVOList = new ArrayList<>();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVO orderItemVO = PoToVoUtil.orderItemToOrderItemVO(orderItem);
            itemVOList.add(orderItemVO);
        }
        return itemVOList;
    }

    /*创建一个订单详情对象*/
    private List<OrderItem> getOrderItem(Integer uid, Long orderNo, List<Product> productList, List<Cart> li) {
        List<OrderItem> itemList = new ArrayList<>();
        //如果orderItem创建到for循环外面经过遍历之后只产生一个对象
        for (Cart cart : li) {
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(cart.getQuantity());
            for (Product product : productList) {
                //不能用等号比较，Iteger类型无法用等号对比
                if (product.getId().equals(cart.getProductId())) {
                    orderItem.setUserId(uid);
                    orderItem.setOrderNo(orderNo);
                    orderItem.setProductId(product.getId());
                    orderItem.setProductName(product.getName());
                    orderItem.setProductImage(product.getMainImage());
                    orderItem.setCurrentUnitPrice(product.getPrice());
                    //根据购物车购物数量和商品单价，计算一条购物车信息的总价
                    BigDecimal mul = BigDecimalUtils.mul(product.getPrice().doubleValue(), cart.getQuantity());
                    orderItem.setTotalPrice(mul);

                    itemList.add(orderItem);
                }
            }
        }
        return itemList;
    }

    /*计算订单总价*/
    private BigDecimal getPayment(List<Cart> li) {

        return null;
    }

    /*生成订单编号*/
    private Long getOrderNo() {
        long l = System.currentTimeMillis();
        Long orderNo = l + Math.round(Math.random() * 100);
        return orderNo;
    }
}
