/*
 * echoAuthorizationVo.java
 * Created at 2020/2/25
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class EchoAuthorizationVo {
    /**
     * 组id
     */
    private String groupId;

    /**
     * 模块名
     */
    private String modularName;

    /**
     * 查看列表  封装数据
     */
    private Map<String, List<PermitValueVo>> data = new HashMap<>();
}
