package com.broadtext.ycadp.data.ac.api.entity;

import com.broadtext.ycadp.base.entity.AbstractBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

/**
 * 数据源实体
 *
 * @author xuchenglong
 */
@Entity
@Table(name = "T_B_DATASOURCE_CONFIG")
@Data
@EqualsAndHashCode(callSuper = true)
public class TBDatasourceConfig extends AbstractBaseEntity{
    /** 数据源名称 */
    private String datasourceName;
    /** 数据源类型 */
    private String datasourceType;
    /** 数据库名(MYSQL)/模式名(ORACLE)/架构名(DB2) */
    private String schemaDesc;
    /** 数据源用户名 */
    private String datasourceUserName;
    /** 数据源密码 */
    private String datasourcePasswd;
    /** 数据库驱动类 */
    private String datasourceDriverClass;
    /** 字典SQL */
    private String dictSql;
    /** 备注 */
    private String remark;
    /** 连接ip地址 */
    private String connectionIp;
    /** 连接端口号 */
    private String connectionPort;
    /** 表数量 */
    private String tableCount;
    /** 文件ID */
    private String fileId;
    /** DB2模式 */
    private String db2Schema;

}
