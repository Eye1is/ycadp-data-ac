/*
 * PermitVo.java
 * Created at 2020/2/21
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PermitVo {
    /**
     * 操作名
     */
    private String operateName;
    /**
     * 操作编号
     */
    private String operateCode;
}
