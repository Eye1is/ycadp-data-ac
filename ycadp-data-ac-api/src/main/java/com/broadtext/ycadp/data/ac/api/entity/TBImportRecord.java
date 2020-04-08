package com.broadtext.ycadp.data.ac.api.entity;

import com.broadtext.ycadp.base.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 导入记录实体
 *
 * @author xuchenglong
 */
@Entity
@Table(name = "T_B_IMPORT_RECORD")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TBImportRecord extends AbstractBaseEntity {
    /** 数据源id */
    @Column(name = "DATASOURCE_ID")
    private String datasourceId;
    /** 文件名称 */
    @Column(name = "FILE_NAME")
    private String fileName;
    /** 文件大小 */
    @Column(name = "FILE_SIZE")
    private String fileSize;
    /** fastdfs文件地址 */
    @Column(name = "CLOUD_URL")
    private String cloudUrl;
}
