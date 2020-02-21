/*
 * Authorization.java
 * Created at 2020/2/20
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.repository;

import com.broadtext.ycadp.core.common.repository.BaseRepository;
import com.broadtext.ycadp.data.ac.api.entity.TBAclDetail;
import com.broadtext.ycadp.data.ac.api.vo.PermitVo;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

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

    /**
     * 根据accessor,groupId,modularName查询
     * @param accessor 用户id(访问对象)
     * @param groupId 用户id(访问对象)
     * @param modularName 用户id(访问对象)
     * @return 权限集合
     */
    @Query(value = "select c.OPERATE_NAME,c.OPERATE_CODE from T_B_ACL_DETAIL a " +
            "join T_R_PERMIT_ROLE b on a.PERMIT_POLICY_ID = b.POLICY_ID " +
            "join T_B_PERMIT_CONTRAST c on b.CONTRAST_ID = c.ID where " +
            "a.ACCESSOR = :accessor and a.GROUP_ID = :groupId and a.MODULAR_NAME = :modularName", nativeQuery = true)
    List<Map<String, String>>  findAuthorizationListWithAccessor(String accessor, String groupId, String modularName);
}
