package com.broadtext.ycadp.data.ac.provider.repository;

import com.broadtext.ycadp.core.common.repository.BaseRepository;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceGroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 组Repository
 *
 * @author xuchenglong
 */
public interface DataacGroupRepository extends BaseRepository<TBDatasourceGroup,String>{
    /**
     * 获取排序之后的组list
     * @return
     */
    @Query(value = "SELECT * FROM T_B_DATASOURCE_GROUP ORDER BY SORT_NUM", nativeQuery = true)
    List<TBDatasourceGroup> getListBySortNum();

    /**
     * 通过名称获取
     * @return
     */
    @Query(value = "SELECT * FROM T_B_DATASOURCE_GROUP WHERE GROUP_NAME = :name", nativeQuery = true)
    List<TBDatasourceGroup> findByName(@Param("name") String name);
}
