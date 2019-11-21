package com.broadtext.ycadp.data.ac.provider.repository;

import com.broadtext.ycadp.core.common.repository.BaseRepository;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceGroup;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 组Repository
 *
 * @author xuchenglong
 */
public interface DataacGroupRespository extends BaseRepository<TBDatasourceGroup,String>{
    /**
     * 获取排序之后的组list
     * @return
     */
    @Query(value = "SELECT * FROM T_B_DATASOURCE_GROUP ORDER BY SORT_NUM", nativeQuery = true)
    List<TBDatasourceGroup> getListBySortNum();
}
