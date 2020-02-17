package com.broadtext.ycadp.data.ac.provider.service;

import com.broadtext.ycadp.core.common.service.BaseService;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceExcel;

import java.util.List;

/**
 * excel数据源映射关系表service类
 *
 * @author xuchenglong
 */
public interface DataExcelService extends BaseService<TBDatasourceExcel, String> {
    /**
     * 根据数据源id删除excel数据源映射关系
     * @param id
     */
    void deleteByDatasourceId(String id);

    /**
     * 根据数据源id查找excel数据源映射关系
     * @param datasourceId
     * @return
     */
    List<TBDatasourceExcel> getListByDataSourceId(String datasourceId);

    /**
     * 根据数据源id和sheetName获取实体
     * @param id
     * @param sheetName
     * @return
     */
    TBDatasourceExcel findByIdAndSheetName(String id, String sheetName);
}
