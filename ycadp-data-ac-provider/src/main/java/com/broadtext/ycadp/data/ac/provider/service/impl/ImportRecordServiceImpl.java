package com.broadtext.ycadp.data.ac.provider.service.impl;

import com.broadtext.ycadp.core.common.service.BaseServiceImpl;
import com.broadtext.ycadp.data.ac.provider.repository.ImportRecordRepository;
import com.broadtext.ycadp.data.ac.provider.service.ImportRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.broadtext.ycadp.data.ac.api.entity.TBImportRecord;
import javax.transaction.Transactional;
import java.util.List;

/**
 * 导入记录服务实现类
 *
 * @author xuchenglong
 */
@Service
@Transactional
public class ImportRecordServiceImpl extends BaseServiceImpl<TBImportRecord, String, ImportRecordRepository> implements ImportRecordService {
    @Autowired
    ImportRecordRepository importRecordRepository;

    @Override
    public void deleteByDatasourceId(String datasourceId) {
        importRecordRepository.deleteByDatasourceId(datasourceId);
    }

    @Override
    public List<TBImportRecord> getListOrderByTime(String datasourceId) {
        return importRecordRepository.findAllByDatasourceId(datasourceId);
    }
}
