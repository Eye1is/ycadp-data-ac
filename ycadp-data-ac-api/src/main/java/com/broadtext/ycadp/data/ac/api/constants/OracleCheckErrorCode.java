/*
 * CheckErrorCode.java
 * Created at 2019/6/24
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.constants;

/**
 * 测试数据源错误码常量类
 * @author ouhaoliang
 */
public final class OracleCheckErrorCode {

    private OracleCheckErrorCode() {
    }

    /**
     * 错误码:错误的数据库名
     */
    public static final Integer ERROR_DATASOURCE = 0;
    /**
     * 错误码:错误的用户名或密码
     */
    public static final Integer ERROR_USERORPW = 1017;
    /**
     * 错误码:无效的权限
     */
    public static final Integer ERROR_ACCESS = 0;
    /**
     * 错误码:错误的连接
     */
    public static final Integer ERROR_CONNECTION = 0;
}
