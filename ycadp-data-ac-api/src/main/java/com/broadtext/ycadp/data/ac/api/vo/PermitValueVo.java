/*
 * PermitValueVo.java
 * Created at 2020/2/19
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PermitValueVo {
    /**
     * 权限详情id
     */
    private String id;

    /**
     * 权限访问用户名称
     */
    private String name;

    /**
     * 1 用户 2 部门 3 组 4，部门组
     */
    private String type;

    /**
     * 组织机构id
     */
    private String orgId;

    /**
     * 组织机构父id
     */
    private String orgParentId;

    /**
     * 组id
     */
    private String groupId;
}
