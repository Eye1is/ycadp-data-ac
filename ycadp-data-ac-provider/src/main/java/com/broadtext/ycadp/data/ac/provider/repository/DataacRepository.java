package com.broadtext.ycadp.data.ac.provider.repository;

import com.broadtext.ycadp.core.common.repository.BaseRepository;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

/**
 * 数据接入Repository
 *
 * @author xuchenglong
 */
public interface DataacRepository extends BaseRepository<TBDatasourceConfig,String> {
    /**
     * 根据datasourceName查询数据源(模糊查询)
     * @param datasourceName
     * @return
     */
    @Query(value = "SELECT * FROM T_B_DATASOURCE_CONFIG WHERE DATASOURCE_NAME LIKE CONCAT('%', :datasourceName, '%')", nativeQuery = true)
    List<TBDatasourceConfig> findByDatasourceName(@Param("datasourceName") String datasourceName);


    @Query(value = "SELECT * FROM T_B_DATASOURCE_CONFIG WHERE PACKAGE_ID = :packageId", nativeQuery = true)
    List<TBDatasourceConfig> findByPackageId(String packageId);
}
