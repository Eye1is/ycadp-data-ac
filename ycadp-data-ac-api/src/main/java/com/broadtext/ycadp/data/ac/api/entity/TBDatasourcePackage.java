package com.broadtext.ycadp.data.ac.api.entity;

import com.broadtext.ycadp.base.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 数据源包实体
 *
 * @author xuchenglong
 */
@Entity
@Table(name = "T_B_DATASOURCE_PACKAGE")
@Data
@EqualsAndHashCode(callSuper = true)
public class TBDatasourcePackage extends AbstractBaseEntity {
    /** 包名称 */
    @Column(name = "PACKAGE_NAME")
    private String packageName;
    /** 组id */
    @Column(name = "GROUP_ID")
    private String groupId;
    /** 包排序 */
    @Column(name = "SORT_NUM")
    private String sortNum;
}
