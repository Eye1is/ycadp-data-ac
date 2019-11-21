package com.broadtext.ycadp.data.ac.api.entity;

import com.broadtext.ycadp.base.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 数据源组实体
 *
 * @author xuchenglong
 */
@Entity
@Table(name = "T_B_DATASOURCE_GROUP")
@Data
@EqualsAndHashCode(callSuper = true)
public class TBDatasourceGroup extends AbstractBaseEntity {
    /** 组名称 */
    @Column(name = "GROUP_NAME")
    private String groupName;
    /** 组排序 */
    @Column(name = "SORT_NUM")
    private String sortNum;
}
