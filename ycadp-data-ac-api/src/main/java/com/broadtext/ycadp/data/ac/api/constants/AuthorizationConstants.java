/*
 * DriveConstants.java
 * Created at May 28, 2019
 * Created by kyh
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.constants;

/**
 * 网盘常量类
 *
 * @author kyh
 * @version 1.0
 */

public final class AuthorizationConstants {

    private AuthorizationConstants() {
    }

    /**
     * 网盘查看权限
     */
    public static final String COLLABORATION_DROVE_VIE_WALL = "collaboration.drive.viewall";
    /**
     * 网盘添加权限
     */
    public static final String COLLABORATION_DRIVE_ADD = "collaboration.drive.add";
    /**
     * 网盘重命名
     */
    public static final String COLLABORATION_DROVE_RENAME = "collaboration.drive.rename";
    /**
     * 网盘删除
     */
    public static final String COLLABORATION_DROVE_DELETE = "collaboration.drive.delete";
    /**
     * authorize权限
     */
    public static final String COLLABORATION_DROVE_AUTHORIZE = "collaboration.drive.authorize";

    /**
     * 用户授权
     */
    public static final String USER_TYPE_PERMIT = "1";

    /**
     * 部门授权
     */
    public static final String ORG_TYPE_PERMIT = "2";


    /**
     * 组授权
     */
    public static final String USER_GROUP_PERMIT = "3";

    /**
     * 部门组授权
     */
    public static final String USER_ORG_GROUP_PERMIT = "4";

    /**
     * 部门组 授权的类型的 的分隔符号
     */
    public static final String ORG_AND_GROUP_PERMIT_SPLIT_CHARACTOR = ":";
}
