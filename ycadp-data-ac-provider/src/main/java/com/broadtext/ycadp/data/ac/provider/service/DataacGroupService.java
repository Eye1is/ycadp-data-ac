package com.broadtext.ycadp.data.ac.provider.service;

import com.broadtext.ycadp.core.common.service.BaseService;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceGroup;

import java.util.List;

/**
 * 组service类
 *
 * @author xuchenglong
 */
public interface DataacGroupService extends BaseService<TBDatasourceGroup,String>{
    /**
     * 获取排序之后的组list
     * @return
     */
    List<TBDatasourceGroup> getListBySortNum();
    /**
     * 根据数据源名称组查找实体
     * @param name
     * @return
     */
    List<TBDatasourceGroup> findByName(String name);
}
