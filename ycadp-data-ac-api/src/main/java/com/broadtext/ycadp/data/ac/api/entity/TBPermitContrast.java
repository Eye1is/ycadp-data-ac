package com.broadtext.ycadp.data.ac.api.entity;

import com.broadtext.ycadp.base.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 权限对照实体
 * @author qiaoyanbo
 */
@Entity
@Table(name = "T_B_PERMIT_CONTRAST")
@Data
@EqualsAndHashCode(callSuper = true)
public class TBPermitContrast extends AbstractBaseEntity {
    /** 模块名称 */
    @Column(name = "MODULAR_NAME")
    private String modularName;
    /** 操作名称 */
    @Column(name = "OPERATE_NAME")
    private String operateName;
    /** 操作code */
    @Column(name = "OPERATE_CODE")
    private String operateCode;
}
