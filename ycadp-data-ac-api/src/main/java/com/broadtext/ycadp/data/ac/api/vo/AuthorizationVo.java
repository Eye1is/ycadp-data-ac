/*
 * AuthorizationVo.java
 * Created at 2020/2/19
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class AuthorizationVo {
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

    /**
     * 保存权限  接收
     */
    private List<PermitValueVo> managers = new ArrayList<>();
    /**
     * 保存权限  接收
     */
    private List<PermitValueVo> editors = new ArrayList<>();
    /**
     * 保存权限  接收
     */
    private List<PermitValueVo> viewers = new ArrayList<>();
}
