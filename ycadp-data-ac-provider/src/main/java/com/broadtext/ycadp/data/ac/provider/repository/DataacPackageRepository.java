package com.broadtext.ycadp.data.ac.provider.repository;

import com.broadtext.ycadp.core.common.repository.BaseRepository;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourcePackage;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 包Repository
 *
 * @author xuchenglong
 */
public interface DataacPackageRepository extends BaseRepository<TBDatasourcePackage,String>{
    /**
     * 获取跟某个groupID对应的经过排序的包list
     * @param groupId
     * @return
     */
    @Query(value = "SELECT * FROM T_B_DATASOURCE_PACKAGE WHERE GROUP_ID=:groupId ORDER BY SORT_NUM", nativeQuery = true)
    List<TBDatasourcePackage> getOrderedListByGroupId(@Param("groupId") String groupId);

    /**
     * 根据groupId删除包
     * @param groupId
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM T_B_DATASOURCE_PACKAGE WHERE GROUP_ID= :groupId", nativeQuery = true)
    void removePackageByGroupId(@Param("groupId") String groupId);

    /**
     * 通过名称获取
     * @return
     */
    @Query(value = "SELECT * FROM T_B_DATASOURCE_PACKAGE WHERE GROUP_ID= :groupId", nativeQuery = true)
    List<TBDatasourcePackage> findByGroupId(@Param("groupId") String groupId);
}
