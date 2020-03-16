/*
 * CryptField.java
 * Created at 2020/3/16
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.annotation;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import java.lang.annotation.*;
/**
 * 字段加解密
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Order(Ordered.HIGHEST_PRECEDENCE)
public @interface CryptField {}