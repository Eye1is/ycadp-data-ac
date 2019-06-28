package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;

/**
 * 数据源列表业务类
 * @author PC-Xuchenglong
 */
@Data
public class DataSourceListVo {
    /** 主键id */
    private String id;
    /** 数据源名称 */
    private String datasourceName;
    /** 数据源名称 */
    private String datasourceType;
}
