package com.broadtext.ycadp.data.ac.api.entity;

import com.broadtext.ycadp.base.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 权限详情实体
 * @author qiaoyanbo
 */
@Entity
@Table(name = "T_B_ACL_DETAIL")
@Data
@EqualsAndHashCode(callSuper = true)
public class TBAclDetail extends AbstractBaseEntity {

    /** 权限策略ID */
    @Column(name = "PERMIT_POLICY_ID")
    private String permitPolicyId;

    /** 访问对象 */
    @Column(name = "ACCESSOR")
    private String accessor;

    /** 类型 1用户2组 */
    @Column(name = "TYPE")
    private String type;

    /** 模块名(如:数据接入dataac，数据资产dataas,报表配置datareportconfig) */
    @Column(name = "MODULAR_NAME")
    private String modularName;

    /** 组id */
    @Column(name = "GROUP_ID")
    private String groupId;
}
