package com.broadtext.ycadp.data.ac.provider.repository;

import com.broadtext.ycadp.core.common.repository.BaseRepository;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import org.springframework.data.repository.query.Param;


import java.util.List;

/**
 * 数据接入Repository
 *
 * @author xuchenglong
 */
public interface DataacRepository extends BaseRepository<TBDatasourceConfig,String> {
    /**
     * 根据datasourceName查询数据源
     * @param datasourceName
     * @return
     */
    List<TBDatasourceConfig> findAllByDatasourceName(@Param("datasourceName") String datasourceName);
}
