package com.broadtext.ycadp.data.ac.api.entity;

import com.broadtext.ycadp.base.entity.AbstractBaseEntity;
import com.broadtext.ycadp.data.ac.api.annotation.CryptField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * 数据源实体
 *
 * @author xuchenglong
 */
@Entity
@Table(name = "T_B_DATASOURCE_CONFIG")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TBDatasourceConfig extends AbstractBaseEntity{
    /** 数据包ID*/
    @Column(name = "PACKAGE_ID")
    private String packageId;
    /** 数据源名称 */
    @Column(name = "DATASOURCE_NAME")
    private String datasourceName;
    /** 数据源类型 */
    @Column(name = "DATASOURCE_TYPE")
    private String datasourceType;
    /** 数据库名(MYSQL)/模式名(ORACLE)/架构名(DB2)/excel文件名 */
    @Column(name = "SCHEMA_DESC")
    private String schemaDesc;
    /** 数据源用户名 */
    @Column(name = "DATASOURCE_USER_NAME")
    private String datasourceUserName;
    /** 数据源密码 */
    @CryptField
    @Column(name = "DATASOURCE_PASSWD")
    private String datasourcePasswd;
    /** 数据库驱动类 */
    @Column(name = "DATASOURCE_DRIVER_CLASS")
    private String datasourceDriverClass;
    /** 字典SQL */
    @Column(name = "DICT_SQL")
    private String dictSql;
    /** 备注 */
    @Column(name = "REMARK")
    private String remark;
    /** 连接ip地址 */
    @Column(name = "CONNECTION_IP")
    private String connectionIp;
    /** 连接端口号 */
    @Column(name = "CONNECTION_PORT")
    private Integer connectionPort;
    /** 表数量 */
    @Column(name = "TABLE_COUNT")
    private String tableCount;
    /** 云地址 */
    @Column(name = "CLOUD_URL")
    private String cloudUrl;
    /** DB2模式 */
    @Column(name = "DB2_SCHEMA")
    private String db2Schema;
    /** 排序字段 */
    @Column(name = "SORT_NUM")
    private String sortNum;
    /** 编码 */
    @Column(name = "CODE")
    private String code;

}
