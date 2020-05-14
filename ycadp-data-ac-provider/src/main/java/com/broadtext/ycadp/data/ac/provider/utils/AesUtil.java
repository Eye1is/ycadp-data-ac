/*
 * AseUtil.java
 * Created at 2020/3/16
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */
package com.broadtext.ycadp.data.ac.provider.utils;

import cn.hutool.crypto.SecureUtil;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * Aes加密
 */
public class AesUtil {

    /**
     * AES 加密操作
     *
     * @param content  待加密内容
     * @param secKey 加密密码
     * @return 返回Base64转码后的加密数据
     */
    public static String encrypt(String content, String secKey) {
        try {
            return SecureUtil.aes(secKey.getBytes()).encryptHex(content);
        } catch (Exception ex) {
            Logger.getLogger(AesUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * AES 解密操作
     *
     * @param content
     * @param secKey
     * @return
     */
    public static String decrypt(String content, String secKey) {
        try {
            return SecureUtil.aes(secKey.getBytes()).decryptStr(content);
        } catch (Exception ex) {
            Logger.getLogger(AesUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

//    /**
//     * 生成加密秘钥
//     *
//     * @return
//     */
//    private static SecretKeySpec getSecretKey(final String password) {
//        //返回生成指定算法密钥生成器的 KeyGenerator 对象
//        KeyGenerator kg = null;
//        try {
//            kg = KeyGenerator.getInstance("AES");
//            //AES 要求密钥长度为 128
//            kg.init(128, new SecureRandom(password.getBytes()));
//            //生成一个密钥
//            SecretKey secretKey = kg.generateKey();
//            return new SecretKeySpec(secretKey.getEncoded(), "AES");// 转换为AES专用密钥
//        } catch (NoSuchAlgorithmException ex) {
//            Logger.getLogger(AesUtil.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
}