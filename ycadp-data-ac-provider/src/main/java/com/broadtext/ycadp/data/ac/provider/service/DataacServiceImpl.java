package com.broadtext.ycadp.data.ac.provider.service;

import com.broadtext.ycadp.core.common.service.BaseServiceImpl;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.utils.DaoFactory;
import com.broadtext.ycadp.data.ac.provider.repository.DataacRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据接入服务实现类
 *
 * @author xuchenglong
 */
@Service
@Transactional
public class DataacServiceImpl extends BaseServiceImpl<TBDatasourceConfig,String,DataacRepository> implements DataacService{
    @Autowired
    private DataacRepository dataacRepository;

    @Override
    public List<TBDatasourceConfig> getListByDatasourceName(String datasourceName) {
        return dataacRepository.findAllByDatasourceName(datasourceName);
    }
    
    @Override
    public Map<Boolean,String> getConnectResult(TBDatasourceConfig config) {
    	boolean result = false;
    	DaoFactory daoFactory = DaoFactory.getDaoFactory(config);
    	Map<Boolean,String> map = new HashMap<Boolean,String>();
    	map = daoFactory.check(config);
    	return map;
    }
}
