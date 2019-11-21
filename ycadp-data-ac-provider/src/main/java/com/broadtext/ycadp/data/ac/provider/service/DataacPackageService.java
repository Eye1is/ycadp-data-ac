package com.broadtext.ycadp.data.ac.provider.service;

import com.broadtext.ycadp.core.common.service.BaseService;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourcePackage;

import java.util.List;

/**
 * 包service类
 *
 * @author xuchenglong
 */
public interface DataacPackageService extends BaseService<TBDatasourcePackage,String>{
    /**
     * 获取与某个groupID对应的经过排序的包list
     * @param groupId
     * @return
     */
    List<TBDatasourcePackage> getOrderedListByGroupId(String groupId);
}
