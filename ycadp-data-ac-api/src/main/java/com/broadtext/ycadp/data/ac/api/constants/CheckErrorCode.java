/*
 * CheckErrorCode.java
 * Created at 2019/6/24
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.constants;

public final class CheckErrorCode {

    private CheckErrorCode() {
    }

    public static final Integer ERROR_DATASOURCE = 1049;
    public static final Integer ERROR_USERORPW = 1045;
    public static final Integer ERROR_ACCESS = 1142;
    public static final Integer ERROR_CONNECTION = 0;
}
