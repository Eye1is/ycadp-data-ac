package com.broadtext.ycadp.data.ac.provider.service;

import com.broadtext.ycadp.core.common.service.BaseService;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;

import java.util.List;
import java.util.Map;

/**
 * 数据接入service类
 *
 * @author xuchenglong
 */
public interface DataacService extends BaseService<TBDatasourceConfig,String>{
    /**
     * 根据数据源名称查找数据源实体
     * @param datasourceName
     * @return
     */
    List<TBDatasourceConfig> getListByDatasourceName(String datasourceName);
}
