/*
 * DataPermitService.java
 * Created at 2020/2/19
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.service.authorization;

import com.broadtext.ycadp.data.ac.api.vo.AuthorizationVo;

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
    AuthorizationVo findDriverAuthorizationList(String groupId, String modularName);
}
