package com.itdr.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import lombok.Getter;
import lombok.Setter;


import java.io.Serializable;
//实现Serializable接口是为了保证序列化的时候不出现问题
//<T>指泛型类
@Getter
@Setter
//msg为null不需要返回的注释
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)//如果类里参数的值为null，往前端序列化是不传值
public class ServerResponse<T> implements Serializable {
    private Integer status;
    private T data;
    private String msg;

    //获取成功的对象，包括成功状态码和数据
    private ServerResponse(T data){
        this.status = 200;
        this.data = data;
    }
    //获取成功的对象，包括成功状态码和数据
    private ServerResponse(Integer status,T data){
        this.status = status;
        this.data = data;
    }
    //获取成功的对象，包括成功的状态码、数据和状态信息
    private ServerResponse(Integer status,T data,String msg){
        this.status = status;
        this.data = data;
        this.msg = msg;
    }
    //获取失败的对象，包括状态码和状态信息
    private ServerResponse(Integer status,String msg){
        this.status = status;
        this.msg = msg;
    }
    //只获取失败的对象，只包括失败信息
    private ServerResponse(String msg){
        this.msg = msg;
    }

    //成功的时候只返回状态码
    public static <T> ServerResponse successRS(){return new ServerResponse(com.itdr.common.Const.SUCCESS);}
    //成功的时候只传入状态码和数据
    public static <T> ServerResponse successRS(String msg) {
        return new ServerResponse(com.itdr.common.Const.SUCCESS,msg);
    }
    //成功的时候只传入数据
    public static <T> ServerResponse successRS(T data) {
        return new ServerResponse(com.itdr.common.Const.SUCCESS,data);
    }
    //成功的时候传入状态码、数据、信息
    public static <T> ServerResponse successRS(Integer status,T data,String msg) {
        return new ServerResponse(com.itdr.common.Const.SUCCESS,data,msg);
    }

    //失败的时候只传入状态码和信息
    public static <T> ServerResponse defeatedRS(Integer status,String msg) {
        return new ServerResponse(status, msg);
    }
    //失败的时候只传入信息
    public static <T> ServerResponse defeatedRS(String msg) {
        return new ServerResponse(com.itdr.common.Const.ERROR,msg);
    }

    //判断是否是成功的方法
    @JsonIgnore//保证返回信息为空时不显示
    public boolean isSuccess(){
        return this.status == com.itdr.common.Const.SUCCESS;
    }
}
