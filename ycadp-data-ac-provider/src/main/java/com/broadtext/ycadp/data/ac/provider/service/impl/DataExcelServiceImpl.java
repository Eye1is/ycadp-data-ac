package com.broadtext.ycadp.data.ac.provider.service.impl;

import com.broadtext.ycadp.core.common.service.BaseServiceImpl;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceExcel;
import com.broadtext.ycadp.data.ac.provider.repository.DataExcelRepository;
import com.broadtext.ycadp.data.ac.provider.service.DataExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * excel数据源映射关系服务实现类
 *
 * @author xuchenglong
 */
@Service
@Transactional
public class DataExcelServiceImpl extends BaseServiceImpl<TBDatasourceExcel, String, DataExcelRepository> implements DataExcelService {
    @Autowired
    private DataExcelRepository excelRepository;

    @Override
    public void deleteByDatasourceId(String id) {
        excelRepository.deleteByDatasourceId(id);
    }

    @Override
    public List<TBDatasourceExcel> getListByDataSourceId(String datasourceId) {
        return excelRepository.findAllByDatasourceId(datasourceId);
    }

    @Override
    public TBDatasourceExcel findByIdAndSheetName(String id, String sheetName) {
        return excelRepository.findByIdAndSheetName(id, sheetName);
    }
}
