/*
 * Authorization.java
 * Created at 2020/2/20
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.repository;

import com.broadtext.ycadp.core.common.repository.BaseRepository;
import com.broadtext.ycadp.data.ac.api.entity.TBAclDetail;

import java.util.List;

public interface AuthorizationRepository extends BaseRepository<TBAclDetail, String> {
    /**
     * 根据groupId删除
     * @param groupId 组id
     */
    void deleteByGroupId(String groupId);

    /**
     * 根据groupId和modularName查询
     * @param groupId 组id
     * @param modularName 模块名
     * @return DriveAclDetail列表
     */
    List<TBAclDetail> findAllByGroupIdAndModularName(String groupId, String modularName);
}
