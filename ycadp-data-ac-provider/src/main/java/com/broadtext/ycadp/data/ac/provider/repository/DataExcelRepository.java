package com.broadtext.ycadp.data.ac.provider.repository;

import com.broadtext.ycadp.core.common.repository.BaseRepository;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceExcel;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * excel数据源映射关系表Repository
 *
 * @author xuchenglong
 */
public interface DataExcelRepository extends BaseRepository<TBDatasourceExcel, String> {

    /**
     * 根据数据源id删除excel数据源映射关系
     * @param id
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM T_R_DATASOURCE_EXCEL WHERE DATASOURCE_ID = :id" , nativeQuery = true)
    void deleteByDatasourceId(@Param(value = "id") String id);

    /**
     * 根据数据源id查找excel数据源映射关系
     * @param datasourceId
     * @return
     */
    List<TBDatasourceExcel> findAllByDatasourceId(String datasourceId);

    /**
     * 根据数据源id和sheetName查找实体
     * @param id
     * @param sheetName
     * @return
     */
    @Query(value = "SELECT * FROM T_R_DATASOURCE_EXCEL WHERE DATASOURCE_ID = :id AND SHEET_NAME= :sheetName" , nativeQuery = true)
    TBDatasourceExcel findByIdAndSheetName(@Param(value = "id")String id, @Param(value = "sheetName")String sheetName);
}
