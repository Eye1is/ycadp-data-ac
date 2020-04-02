package com.broadtext.ycadp.data.ac.provider.service;

import com.broadtext.ycadp.core.common.service.BaseService;
import com.broadtext.ycadp.data.ac.api.entity.TBImportRecord;

import java.util.List;

/**
 * excel导入记录service
 *
 * @author xuchenglong
 */
public interface ImportRecordService extends BaseService<TBImportRecord, String> {
    /**
     * 根据数据源id删除导入记录
     * @param datasourceId
     */
    void deleteByDatasourceId(String datasourceId);

    /**
     * 根据数据源id查找列表  时间倒序
     * @param datasourceId
     * @return
     */
    List<TBImportRecord> getListOrderByTime(String datasourceId);
}
