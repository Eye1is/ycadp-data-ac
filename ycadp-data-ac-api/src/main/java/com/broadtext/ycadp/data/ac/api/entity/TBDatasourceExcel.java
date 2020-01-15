package com.broadtext.ycadp.data.ac.api.entity;

import com.broadtext.ycadp.base.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * excel数据源映射关系实体
 *
 * @author xuchenglong
 */
@Entity
@Table(name = "T_R_DATASOURCE_EXCEL")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TBDatasourceExcel extends AbstractBaseEntity {
    /** 数据源ID*/
    @Column(name = "DATASOURCE_ID")
    private String datasourceId;
    /** sheet名称 */
    @Column(name = "SHEET_NAME")
    private String sheetName;
    /** 映射sheet表名称 */
    @Column(name = "SHEET_TABLE_NAME")
    private String sheetTableName;
}
