package com.shop.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * MD5加密工具类
 * 2次MD5加密：
 *  1) 客户端 固定salt MD5
 *  2) 服务端 随机salt MD5
 */
public class MD5Util {

    private static final String SALT = "1Sec2Kill3Shop4";

    private static String md5(String str){
        return DigestUtils.md5Hex(str);
    }

    public static String md5WithSalt(String str, String salt){
        int saltLen = salt.length();
        return md5(salt.substring(0, saltLen/2) + str + salt.substring(saltLen/2));
    }

    public static String md5WithFixedSalt(String str){
        return md5WithSalt(str, SALT);
    }

    public static void main(String[] args) {
        System.out.println(md5WithSalt(md5WithFixedSalt("123456"), "mysalt") );
    }
}
