/*
 * BrokerAspect.java
 * Created at 2020/3/16
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */
package com.broadtext.ycadp.data.ac.provider.utils;

import com.broadtext.ycadp.data.ac.api.annotation.CryptField;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * 安全字段加密解密切面
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Aspect
@Component
public class CryptFieldAspect {
    private static Logger logger = LoggerFactory.getLogger(CryptFieldAspect.class);
    @Value("${crypt.seckey}")
    private String secretKey;

    @Pointcut("@annotation(com.broadtext.ycadp.data.ac.api.annotation.EncryptMethod)")
    public void encryptPointCut() {
    }

    @Pointcut("@annotation(com.broadtext.ycadp.data.ac.api.annotation.DecryptMethod)")
    public void decryptPointCut() {
    }

    /**
     * 加密数据环绕处理
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("encryptPointCut()")
    public Object aroundEncrypt(ProceedingJoinPoint joinPoint) throws Throwable {
        Object requestObj = joinPoint.getArgs()[0];
        handleEncrypt(requestObj); // 加密CryptField注解字段
        Object responseObj = joinPoint.proceed(); // 方法执行
        handleDecrypt(responseObj); // 解密CryptField注解字段

        return responseObj;
    }

    /**
     * 解密数据环绕处理
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("decryptPointCut()")
    public Object aroundDecrypt(ProceedingJoinPoint joinPoint) throws Throwable {
        Object requestObj = joinPoint.getArgs()[0];
        handleDecrypt(requestObj); // 解密CryptField注解字段
        Object responseObj = joinPoint.proceed(); // 方法执行
        handleEncrypt(responseObj); // 加密CryptField注解字段
        return responseObj;
    }

    /**
     * 处理加密
     *
     * @param requestObj
     */
    private void handleEncrypt(Object requestObj) throws IllegalAccessException {
        if (Objects.isNull(requestObj)) {
            return;
        }
        Field[] fields = requestObj.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean hasSecureField = field.isAnnotationPresent(CryptField.class);
            if (hasSecureField) {
                field.setAccessible(true);
                String plaintextValue = (String) field.get(requestObj);
                String encryptValue = AesUtil.encrypt(plaintextValue, secretKey);
                field.set(requestObj, encryptValue);
            }
        }
    }

    /**
     * 处理解密
     *
     * @param responseObj
     */
    private Object handleDecrypt(Object responseObj) throws IllegalAccessException {
        if (Objects.isNull(responseObj)) {
            return null;
        }

        Field[] fields = responseObj.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean hasSecureField = field.isAnnotationPresent(CryptField.class);
            if (hasSecureField) {
                field.setAccessible(true);
                String encryptValue = (String) field.get(responseObj);
                String plaintextValue = AesUtil.decrypt(encryptValue, secretKey);
                field.set(responseObj, plaintextValue);
            }
        }
        return responseObj;
    }
}