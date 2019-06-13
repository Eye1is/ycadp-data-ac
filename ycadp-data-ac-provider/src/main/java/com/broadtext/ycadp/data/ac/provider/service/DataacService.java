package com.broadtext.ycadp.data.ac.provider.service;

import com.broadtext.ycadp.core.common.service.BaseService;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;

import java.util.List;

/**
 * 数据接入service类
 *
 * @author xuchenglong
 */
public interface DataacService extends BaseService<TBDatasourceConfig,String>{
    List<TBDatasourceConfig> getListByDatasourceName(String datasourceName);
}
