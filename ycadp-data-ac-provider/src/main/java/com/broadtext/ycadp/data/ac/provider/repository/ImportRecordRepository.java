package com.broadtext.ycadp.data.ac.provider.repository;

import com.broadtext.ycadp.core.common.repository.BaseRepository;
import com.broadtext.ycadp.data.ac.api.entity.TBImportRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * excel数据源导入记录Repository
 *
 * @author xuchenglong
 */
public interface ImportRecordRepository extends BaseRepository<TBImportRecord, String> {
    /**
     * 根据数据源id删除导入记录
     * @param datasourceId
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM T_B_IMPORT_RECORD WHERE DATASOURCE_ID = :datasourceId" , nativeQuery = true)
    void deleteByDatasourceId(@Param(value = "datasourceId") String datasourceId);

    /**
     * 根据数据源id查找列表 根据创建时间倒序
     * @param datasourceId
     * @return
     */
    @Query(value = "SELECT * FROM T_B_IMPORT_RECORD WHERE DATASOURCE_ID = :datasourceId ORDER BY CREATED_TIME DESC" , nativeQuery = true)
    List<TBImportRecord> findAllByDatasourceId(@Param(value = "datasourceId") String datasourceId);
}
