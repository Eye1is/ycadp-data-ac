package com.broadtext.ycadp.data.ac.api.entity;

import com.broadtext.ycadp.base.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 角色权限关联中间实体
 * @author qiaoyanbo
 */
@Entity
@Table(name = "T_R_PERMIT_ROLE")
@Data
@EqualsAndHashCode(callSuper = true)
public class TBPermitRole extends AbstractBaseEntity {
    /** 权限对照表id */
    @Column(name = "CONTRAST_ID")
    private String contrastId;
    /** 角色id */
    @Column(name = "POLICY_ID")
    private String policyId;
}
