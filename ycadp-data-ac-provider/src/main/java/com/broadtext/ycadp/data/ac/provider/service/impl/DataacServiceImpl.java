package com.broadtext.ycadp.data.ac.provider.service.impl;

import com.broadtext.ycadp.core.common.service.BaseServiceImpl;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.provider.repository.DataacRepository;
import com.broadtext.ycadp.data.ac.provider.service.DataacService;;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 数据接入服务实现类
 *
 * @author xuchenglong
 */
@Service
@Transactional
public class DataacServiceImpl extends BaseServiceImpl<TBDatasourceConfig,String,DataacRepository> implements DataacService {
    @Autowired
    private DataacRepository dataacRepository;

    @Override
    public List<TBDatasourceConfig> getListByDatasourceName(String datasourceName) {
        return dataacRepository.findByDatasourceName(datasourceName);
    }

    @Override
    public String getFieldTypeById(String datasourceId) {
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull){
            TBDatasourceConfig datasourceConfig = dataacRepository.getOne(datasourceId);
            return datasourceConfig.getDatasourceType();
        }
        return null;
    }

    @Override
    public List<TBDatasourceConfig> getDatasourceByPackageId(String packageId) {
        return dataacRepository.findByPackageId(packageId);
    }
}
