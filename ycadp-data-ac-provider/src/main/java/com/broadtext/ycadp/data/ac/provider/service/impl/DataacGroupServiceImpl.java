package com.broadtext.ycadp.data.ac.provider.service.impl;

import com.broadtext.ycadp.core.common.service.BaseServiceImpl;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceGroup;
import com.broadtext.ycadp.data.ac.provider.repository.DataacGroupRespository;
import com.broadtext.ycadp.data.ac.provider.service.DataacGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * 组服务实现类
 *
 * @author xuchenglong
 */
@Service
@Transactional
public class DataacGroupServiceImpl extends BaseServiceImpl<TBDatasourceGroup,String,DataacGroupRespository> implements DataacGroupService{
    @Autowired
    private DataacGroupRespository groupRespository;

    @Override
    public List<TBDatasourceGroup> getListBySortNum() {
        return groupRespository.getListBySortNum();
    }
}
