package com.itdr.common;

import javax.management.relation.Role;

public class Const {
    public static final String LOGINUSER = "login_user";
    public static final String TRADE_SUCCESS = "TRADE_SUCCESS";
    public static final String AUTOLOGINTOKEN = "autoLoginToken";
    public static final String JESSESSIONID_COOKIE = "JESSESSIONID_COOKIE";
    public static final String EMAIIL = "email";
    public static final String USERNAME = "username";
    public static final String TOKEN = "token_";
    /*
     * 成功时通用状态码
      * */
    public static final int SUCCESS=200;

    /*
    * 失败时通用状态码
    * */
    public static final int ERROR=100;

    public enum UsersEnum{
        NEED_LOGIN(2,"需要登录"),
        NO_LOGIN(101,"用户未登录");
        //状态信息

        //状态码和状态信息
        private int code;
        private String desc;

        private UsersEnum(int code,String desc){
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

//    public enum RoleEnum{
//        ROLE_ADMIN(0,"管理员"),
//        ROLE_CUSTOMER(1,"普通用户");
//
//        private int code;
//        private String desc;
//
//        private RoleEnum(int code,String desc){
//            this.code = code;
//            this.desc = desc;
//        }
//        public int getCode() {
//            return code;
//        }
//
//        public void setCode(int code) {
//            this.code = code;
//        }
//
//        public String getDesc() {
//            return desc;
//        }
//
//        public void setDesc(String desc) {
//            this.desc = desc;
//        }
//
//    }


}
