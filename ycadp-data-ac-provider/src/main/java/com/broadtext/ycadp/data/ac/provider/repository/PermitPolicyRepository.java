/*
 * PermitPolicyRepository.java
 * Created at 2020/2/20
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.repository;

import com.broadtext.ycadp.core.common.repository.BaseRepository;
import com.broadtext.ycadp.data.ac.api.entity.TBPermitPolicy;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PermitPolicyRepository extends BaseRepository<TBPermitPolicy, String> {
    @Query(value = "SELECT * FROM T_B_PERMIT_POLICY WHERE NAME =:name1 or NAME=:name2", nativeQuery = true)
    public List<TBPermitPolicy> findAllByNames(@Param("name1") String name1, @Param("name2") String name2);
}
