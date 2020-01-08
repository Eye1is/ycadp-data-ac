/*
 * PostgresqlCheckErrorCode.java
 * Created at 2020/1/7
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.constants;

public final class PostgresqlCheckErrorCode {

    private PostgresqlCheckErrorCode() {
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
