/*
 * AuthorizationServiceImpl.java
 * Created at 2020/2/19
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.service.authorization.impl;

import com.broadtext.ycadp.OrgApi;
import com.broadtext.ycadp.UserRoleDeptApi;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.constants.AuthorizationConstants;
import com.broadtext.ycadp.data.ac.api.entity.TBAclDetail;
import com.broadtext.ycadp.data.ac.api.entity.TBPermitContrast;
import com.broadtext.ycadp.data.ac.api.entity.TBPermitPolicy;
import com.broadtext.ycadp.data.ac.api.vo.AuthorizationVo;
import com.broadtext.ycadp.data.ac.api.vo.PermitValueVo;
import com.broadtext.ycadp.data.ac.api.vo.PermitVo;
import com.broadtext.ycadp.data.ac.provider.repository.AuthorizationRepository;
import com.broadtext.ycadp.data.ac.provider.repository.PermitContrastRepository;
import com.broadtext.ycadp.data.ac.provider.repository.PermitPolicyRepository;
import com.broadtext.ycadp.data.ac.provider.service.authorization.AuthorizationService;
import com.broadtext.ycadp.entity.User;
import com.broadtext.ycadp.org.api.vo.TSOrgGroupVo;
import com.broadtext.ycadp.org.api.vo.TSOrgVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@SuppressWarnings("all")
public class AuthorizationServiceImpl implements AuthorizationService {

    @Autowired
    private AuthorizationRepository authorizationRepository;
    @Autowired
    private PermitPolicyRepository permitPolicyRepository;
    @Autowired
    private PermitContrastRepository permitContrastRepository;
    @Autowired
    private OrgApi orgApi;
    @Autowired
    private UserRoleDeptApi userRoleDeptApi;

    @Override
    public AuthorizationVo findAuthorizationList(String groupId, String modularName) {
        Map<String, List<PermitValueVo>> dataMap = new HashMap<>();

        AuthorizationVo authorizationVo = new AuthorizationVo();
        //@Accessors(chain = true) 实体添加此注解可链式设置值
        authorizationVo.setGroupId(groupId).setModularName(modularName);

        packData(groupId, modularName, dataMap);

        authorizationVo.setData(dataMap);

        return authorizationVo;
    }

    /**
     * 根据组id查找该组下所有权限角色对应的用户
     *
     * @param dataMap     数据封装对象
     * @param groupId     组id
     * @param modularName 模块名
     */
    public void packData(String groupId, String modularName, Map<String, List<PermitValueVo>> dataMap) {

        List<TBAclDetail> aclDetails = null;

        aclDetails = authorizationRepository.findAllByGroupIdAndModularName(groupId, modularName);


        Map<String, String> userMap = new HashMap<>();
        Map<String, TSOrgVo> orgMap = new HashMap<>();
        Map<String, TSOrgGroupVo> orgGroupVoMap = new HashMap<>();
        //获取ACL中关联的所有的用户id，组织，组，部门组code，在查看时需要进行转义回显
        buildPermitDisplayInfo(aclDetails, userMap, orgMap, orgGroupVoMap);

        //查询所有权限角色
        Map<String, String> policyMap = new HashMap<>();
        List<TBPermitPolicy> permitPolicies = permitPolicyRepository.findAll();
        for (TBPermitPolicy permitPolicy : permitPolicies) {
            policyMap.put(permitPolicy.getId(), permitPolicy.getName());
        }

        for (TBAclDetail aclDetail : aclDetails) {
            List<PermitValueVo> userVos = null;
            String name = policyMap.get(aclDetail.getPermitPolicyId());
            String type = aclDetail.getType();
            String accessor = aclDetail.getAccessor();
            //按不同权限角色分类
            if (dataMap.containsKey(name)) {
                userVos = dataMap.get(name);
                PermitValueVo userVo = setPermitVoInfo(userMap, orgMap, orgGroupVoMap, type, accessor);
                userVos.add(userVo);
            } else {
                userVos = new ArrayList<>();
                PermitValueVo userVo = setPermitVoInfo(userMap, orgMap, orgGroupVoMap, type, accessor);
                userVos.add(userVo);
                dataMap.put(name, userVos);
            }
        }
    }

    /**
     * 构建权限回显信息
     *
     * @param aclDetails    acl详情
     * @param userMap       用户信息map
     * @param orgMap        组织信息map
     * @param orgGroupVoMap 组信息map
     */
    private void buildPermitDisplayInfo(List<TBAclDetail> aclDetails, Map<String, String> userMap, Map<String, TSOrgVo> orgMap, Map<String, TSOrgGroupVo> orgGroupVoMap) {
        List<String> userIds = new ArrayList<>();
        List<String> orgCodes = new ArrayList<>();
        List<String> groupCodes = new ArrayList<>();

        for (TBAclDetail aclDetail : aclDetails) {
            String type = aclDetail.getType();
            if (null != type && type.equals(AuthorizationConstants.ORG_TYPE_PERMIT)) {
                orgCodes.add(aclDetail.getAccessor());
            }
            if (null != type && type.equals(AuthorizationConstants.USER_TYPE_PERMIT)) {
                userIds.add(aclDetail.getAccessor());
            }
            if (null != type && type.equals(AuthorizationConstants.USER_GROUP_PERMIT)) {
                groupCodes.add(aclDetail.getAccessor());
            }
            //将用户部门组分解来以:分解开，分别去查询
            if (null != type && type.equals(AuthorizationConstants.USER_ORG_GROUP_PERMIT)) {
                if (StringUtils.isNotEmpty(aclDetail.getAccessor())) {
                    String[] orgAndGroup = aclDetail.getAccessor().split(AuthorizationConstants.ORG_AND_GROUP_PERMIT_SPLIT_CHARACTOR);
                    if (orgAndGroup.length == 2) {
                        orgCodes.add(orgAndGroup[0]);
                        groupCodes.add(orgAndGroup[1]);
                    }
                }
            }
        }
        List<TSOrgVo> orgVoList = new ArrayList<>();
        if (orgCodes.size() > 0) {
            RespEntity<List<TSOrgVo>> respEntity = orgApi.findOrgsByOrgCodes(orgCodes);
            if (null != respEntity) {
                orgVoList = respEntity.getData();
            }
        }
        if (groupCodes.size() > 0) {
            RespEntity<List<TSOrgGroupVo>> respEntity = orgApi.findGroupByCodesIn(groupCodes);
            if (null != respEntity) {
                for (TSOrgGroupVo tsOrgGroupVo : respEntity.getData()) {
                    orgGroupVoMap.put(tsOrgGroupVo.getCode(), tsOrgGroupVo);
                }
            }
        }

        List<User> userVoList = userRoleDeptApi.findByIdIn(userIds).getData();
        //将list转换map为了后面显示
        for (TSOrgVo orgVo : orgVoList) {
            orgMap.put(orgVo.getCode(), orgVo);
        }
        for (User user : userVoList) {
            userMap.put(user.getId(), user.getUserName());
        }
    }

    /**
     * 封装权限的vo信息
     *
     * @param userMap    用户信息map
     * @param orgMap     组织code对应的组织机构信息map
     * @param groupVoMap 组map
     * @param type       类型
     * @param accessor   访问用户
     * @return 权限信息
     */
    private PermitValueVo setPermitVoInfo(Map<String, String> userMap, Map<String, TSOrgVo> orgMap, Map<String, TSOrgGroupVo> groupVoMap, String type, String accessor) {
        PermitValueVo permitValueVo = new PermitValueVo().setId(accessor).setType(type);
        if (type.equals(AuthorizationConstants.USER_TYPE_PERMIT)) {
            permitValueVo.setName(userMap.get(accessor));
        }
        if (type.equals(AuthorizationConstants.ORG_TYPE_PERMIT)) {
            TSOrgVo orgVo = orgMap.get(accessor);
            permitValueVo.setName(orgVo.getName()).setOrgId(orgVo.getId()).setOrgParentId(orgVo.getParentId());
        }
        if (type.equals(AuthorizationConstants.USER_GROUP_PERMIT)) {
            permitValueVo.setName(groupVoMap.get(accessor).getName());
            permitValueVo.setGroupId(groupVoMap.get(accessor).getId());
        }
        if (type.equals(AuthorizationConstants.USER_ORG_GROUP_PERMIT)) {
            String[] permitValue = accessor.split(AuthorizationConstants.ORG_AND_GROUP_PERMIT_SPLIT_CHARACTOR);
            if (null != permitValue && permitValue.length == 2) {
                TSOrgVo orgVo = orgMap.get(permitValue[0]);
                TSOrgGroupVo orgGroupVo = groupVoMap.get(permitValue[1]);
                permitValueVo.setName(orgVo.getName() + "-" + orgGroupVo.getName());
                permitValueVo.setOrgId(orgVo.getId()).setOrgParentId(orgVo.getParentId());
                permitValueVo.setGroupId(orgGroupVo.getId());
            }
        }

        return permitValueVo;
    }

    @Override
    public void saveAuthorizations(AuthorizationVo vo) throws Exception {
        authorizationRepository.deleteByGroupId(vo.getGroupId());
        String modularName = vo.getModularName();
        String groupId = vo.getGroupId();
        List<PermitValueVo> managers = vo.getManagers();
        List<PermitValueVo> editors = vo.getEditors();
        List<PermitValueVo> viewers = vo.getViewers();
        Map<String, String> map = new HashMap<>();
        //查找  权限策略表
        List<TBPermitPolicy> PermitPolicies = permitPolicyRepository.findAll();
        for (TBPermitPolicy permitPolicy : PermitPolicies) {
            map.put(permitPolicy.getName(), permitPolicy.getId());
        }

        List<TBAclDetail> list = new ArrayList<>();

        //保存管理员权限
        for (PermitValueVo manager : managers) {
            list.add(packDriveAclDetail(manager, map, "管理员", modularName, groupId));
        }

        //保存编辑者权限
        for (PermitValueVo editor : editors) {
            list.add(packDriveAclDetail(editor, map, "编辑者", modularName, groupId));
        }

        //保存查看者权限
        for (PermitValueVo viewer : viewers) {
            list.add(packDriveAclDetail(viewer, map, "查看者", modularName, groupId));
        }

        authorizationRepository.saveAll(list);
    }

    /**
     * 封装AclDetail
     *
     * @param vo   用户Vo
     * @param map  封装的权限值和名称
     * @param type 权限值类型
     * @return DriveAclDetail
     */
    private TBAclDetail packDriveAclDetail(PermitValueVo vo, Map<String, String> map, String type, String modularName, String groupId) {
        TBAclDetail aclDetail = new TBAclDetail();
        aclDetail.setType(vo.getType());
        aclDetail.setAccessor(vo.getId());
        aclDetail.setPermitPolicyId(map.get(type));
        aclDetail.setGroupId(groupId);
        aclDetail.setModularName(modularName);
        return aclDetail;
    }

    @Override
    public List<Map<String, String>>  findAuthorizationListWithAccessor(String accessor, String groupId, String modularName) {
        return authorizationRepository.findAuthorizationListWithAccessor(accessor,groupId,modularName);
    }

    @Override
    public List<TBPermitContrast> findAllPermitList() {
        return permitContrastRepository.findAll();
    }

    public List<TBPermitPolicy> findPermitPolicyByName(String name1, String name2) {
        return permitPolicyRepository.findAllByNames(name1, name2);
    }

    @Override
    public List<TBAclDetail> findByModulePermitUser(String moduleName, String permitId, String userId) {
        return authorizationRepository.findAllByModulePermitUser(moduleName, permitId, userId);
    }

}
