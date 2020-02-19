package com.broadtext.ycadp.data.ac.api.entity;

import com.broadtext.ycadp.base.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 权限策略实体
 * @author qiaoyanbo
 */
@Entity
@Table(name = "T_B_PERMIT_POLICY")
@Data
@EqualsAndHashCode(callSuper = true)
public class TBPermitPolicy extends AbstractBaseEntity {

    /** 权限策略名称(管理者，编辑者，查看者) */
    @Column(name = "NAME")
    private String name;

    /** 权限策略值 由权限点上的值拼接组成 */
    @Column(name = "PERMIT_VALUE")
    private String permitValue;
}
