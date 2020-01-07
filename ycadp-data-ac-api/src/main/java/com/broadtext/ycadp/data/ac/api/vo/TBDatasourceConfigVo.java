package com.broadtext.ycadp.data.ac.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 数据源配置业务类
 * @author PC-Xuchenglong
 */
@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TBDatasourceConfigVo {
    /** 主键id */
    private String id;
    /** 数据源名称 */
    private String datasourceName = "";
    /** 数据源类型 */
    private String datasourceType = "";
    /** 数据库名(MYSQL)/模式名(ORACLE)/架构名(DB2) */
    private String schemaDesc = "";
    /** 数据源用户名 */
    private String datasourceUserName = "";
    /** 数据源密码 */
    private String datasourcePasswd = "";
    /** 数据库驱动类 */
    private String datasourceDriverClass = "";
    /** 字典SQL */
    private String dictSql = "";
    /** 备注 */
    private String remark = "";
    /** 连接ip地址 */
    private String connectionIp = "";
    /** 连接端口号 */
    private Integer connectionPort;
    /** 表数量 */
    private String tableCount = "";
    /** 云地址 */
    private String cloudUrl = "";
    /** DB2模式 */
    private String db2Schema = "";
    /** 数据包ID*/
    @Builder.Default
    private String packageId = "";
}
