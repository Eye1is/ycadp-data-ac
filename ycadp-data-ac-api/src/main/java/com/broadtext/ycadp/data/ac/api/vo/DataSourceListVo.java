package com.broadtext.ycadp.data.ac.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * 数据源列表业务类
 * @author PC-Xuchenglong
 */
@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DataSourceListVo {
    /** 主键id */
    private String id;
    /** 数据源名称 */
    @Builder.Default
    private String datasourceName = "";
    /** 数据源名称 */
    @Builder.Default
    private String datasourceType = "";
    /** 数据库名(MYSQL)/模式名(ORACLE)/架构名(DB2) */
    @Builder.Default
    private String schemaDesc = "";
    /** 连接ip地址 */
    @Builder.Default
    private String connectionIp = "";
    /** 连接端口号 */
    private Integer connectionPort;
    /** 数据包ID*/
    @Builder.Default
    private String packageId = "";
    /** 创建时间 */
    private Date createdTime;
}
