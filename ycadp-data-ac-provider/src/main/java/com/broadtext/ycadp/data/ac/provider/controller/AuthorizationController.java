/*
 * DataPemitController.java
 * Created at 2020/2/19
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.controller;

import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.entity.TBPermitContrast;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.api.vo.AuthorizationVo;
import com.broadtext.ycadp.data.ac.api.vo.PermitVo;
import com.broadtext.ycadp.data.ac.provider.service.authorization.AuthorizationService;
import com.broadtext.ycadp.data.ac.provider.utils.ArrayUtil;
import com.broadtext.ycadp.util.userutil.CurrentUserUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源权限Controller
 *
 * @author ouhaoliang
 */
@RestController
@Slf4j
public class AuthorizationController {
    @Autowired
    private AuthorizationService authorizationService;


    /**
     * 查询授权列表
     *
     * @param vo      查询条件
     * @param request 请求
     * @return 数据信息
     */
    @GetMapping("/authorization/echo")
    public RespEntity findAuthorizationList(AuthorizationVo vo, HttpServletRequest request) {
        if (vo == null || StringUtils.isEmpty(vo.getGroupId()) || StringUtils.isEmpty(vo.getModularName())) {
            return new RespEntity<>(DataacRespCode.DATAAC_RESP_CODE);
        }
        AuthorizationVo authorizationList = null;
        try {
            authorizationList = authorizationService.findAuthorizationList(vo.getGroupId(), vo.getModularName());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new RespEntity<Object>(DataacRespCode.DATAAC_RESP_CODE);
        }

        return new RespEntity<Object>(RespCode.SUCCESS, authorizationList);
    }

    /**
     * 更新保存权限
     *
     * @param vo 前台数据
     * @return RespEntity 数据信息
     */
    @PostMapping("/authorization/save")
    public RespEntity saveDriverAuthorizations(@RequestBody AuthorizationVo vo) {


        if (vo == null || StringUtils.isEmpty(vo.getGroupId()) || StringUtils.isEmpty(vo.getModularName())) {
            return new RespEntity<>(DataacRespCode.DATAAC_RESP_CODE);
        }

        try {
            authorizationService.saveAuthorizations(vo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new RespEntity<>(DataacRespCode.DATAAC_RESP_CODE);
        }

        return new RespEntity<>(RespCode.SUCCESS);
    }

    @GetMapping("/authorization/resultSearch")
    public RespEntity resultSearch(AuthorizationVo vo, HttpServletRequest request) {
        RespEntity respEntity = null;
        try {
            String groupId = vo.getGroupId();
            String modularName = vo.getModularName();
            String userId = CurrentUserUtils.getUser().getUserId();
            List<PermitVo> permitList;
            Map<String, Object> map = new HashMap<>();
            //查询步骤
            //1.userId就是权限对象,可以查出aclDetail表中的该user的全部实体
            //2.获取它的permitPolicyId集合查询关联的所有contrastId(permitRole表)
            //3.contrastId集合查出所有的operateCode和operateName
            if (StringUtils.isEmpty(userId)) {
                return new RespEntity<>(DataacRespCode.DATAAC_RESP_CODE);
            } else {
                permitList = authorizationService.findAuthorizationListWithAccessor(userId, groupId, modularName);
                if (!ArrayUtil.isEmpty(permitList)){
                    map.put("permitList",permitList);
                    respEntity = new RespEntity<>(RespCode.SUCCESS,map);
                } else {
                    List<TBPermitContrast> permitContrasts = authorizationService.findAllPermitList();
                    List<PermitVo> newPermitList = Lists.newArrayList();
                    for (TBPermitContrast permitContrast : permitContrasts) {
                        PermitVo permitVo = new PermitVo();
                        permitVo.setOperateCode(permitContrast.getOperateCode());
                        permitVo.setOperateName(permitContrast.getOperateName());
                        newPermitList.add(permitVo);
                    }
                    map.put("permitList",newPermitList);
                    respEntity = new RespEntity<>(RespCode.SUCCESS,map);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new RespEntity<>(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }

}
