/*
 * DataPermitService.java
 * Created at 2020/2/19
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.service.authorization;

import com.broadtext.ycadp.data.ac.api.entity.TBAclDetail;
import com.broadtext.ycadp.data.ac.api.entity.TBPermitContrast;
import com.broadtext.ycadp.data.ac.api.entity.TBPermitPolicy;
import com.broadtext.ycadp.data.ac.api.vo.AuthorizationVo;
import com.broadtext.ycadp.data.ac.api.vo.PermitVo;

import java.util.List;
import java.util.Map;

public interface AuthorizationService {

    /**
     * 保存权限
     *
     * @param vo 接收参数
     * @throws Exception 抛出异常
     */
    void saveAuthorizations(AuthorizationVo vo) throws Exception;

    /**
     * 回显权限
     * @param groupId
     * @param modularName
     * @return
     */
    AuthorizationVo findAuthorizationList(String groupId, String modularName);

    /**
     * 查出aclDetail表中的该user的全部实体
     * @param accessor 当前用户id
     * @return 权限集合
     */
    List<Map<String, String>>  findAuthorizationListWithAccessor(String accessor, String groupId, String modularName);

    List<TBPermitContrast> findAllPermitList();

    /**
     *
     * @param name1
     * @param name2
     * @return
     */
    List<TBPermitPolicy> findPermitPolicyByName(String name1, String name2);

    /**
     *
     * @param moduleName
     * @param permitId
     * @param userId
     * @return
     */
    List<TBAclDetail> findByModulePermitUser(String moduleName, String permitId, String userId);
}
