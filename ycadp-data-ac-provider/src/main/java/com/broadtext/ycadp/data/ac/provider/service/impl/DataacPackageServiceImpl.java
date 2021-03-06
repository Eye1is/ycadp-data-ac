package com.broadtext.ycadp.data.ac.provider.service.impl;

import com.broadtext.ycadp.core.common.service.BaseServiceImpl;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourcePackage;
import com.broadtext.ycadp.data.ac.provider.repository.DataacPackageRepository;
import com.broadtext.ycadp.data.ac.provider.service.DataacPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * 包服务实现类
 *
 * @author xuchenglong
 */
@Service
@Transactional
public class DataacPackageServiceImpl extends BaseServiceImpl<TBDatasourcePackage,String, DataacPackageRepository> implements DataacPackageService{
    @Autowired
    private DataacPackageRepository packageRespository;

    @Override
    public List<TBDatasourcePackage> findByGroupId(String groupId) {
        return packageRespository.findByGroupId(groupId);
    }

    @Override
    public List<TBDatasourcePackage> getOrderedListByGroupId(String groupId) {
        return packageRespository.getOrderedListByGroupId(groupId);
    }

    @Override
    public void removePackageByGroupId(String groupId) {
        packageRespository.removePackageByGroupId(groupId);
    }
}
