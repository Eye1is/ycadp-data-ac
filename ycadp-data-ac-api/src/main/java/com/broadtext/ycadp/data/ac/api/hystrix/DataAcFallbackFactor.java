package com.broadtext.ycadp.data.ac.api.hystrix;

import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.DataAcApi;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.vo.DatasourceDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictMapVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictVo;
import com.broadtext.ycadp.data.ac.api.vo.TBDatasourceConfigVo;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 熔断回退类
 *
 * @author PC-Xuchenglong
 */
@Component
public class DataAcFallbackFactor implements FallbackFactory<DataAcApi> {

    @Override
    public DataAcApi create(Throwable cause) {
        cause.printStackTrace();

        return new DataAcApi() {

            @Override
            public RespEntity<Object> addDatasource(TBDatasourceConfigVo datasourceConfig) {
                return null;
            }

            @Override
            public RespEntity<Object> deleteDatasource(String id) {
                return null;
            }

            @Override
            public RespEntity getDatasources(String pageNum, String pageSize) {
                return null;
            }

            @Override
            public RespEntity<TBDatasourceConfig> getDatasource(String id) {
                return null;
            }

            @Override
            public RespEntity<TBDatasourceConfig> updateDatasource(String id, TBDatasourceConfigVo datasourceConfig) {
                return null;
            }

            @Override
            public RespEntity<Map> searchTables(String id, String tableName) {
                return null;
            }

            @Override
            public RespEntity<Map> searchDataTable(String id, String tableName, String pageNum, String pageSize) {
                return null;
            }

            @Override
            public RespEntity<String> connectDatasource(TBDatasourceConfig datasourceConfig) {
                return null;
            }

            @Override
            public RespEntity<List<Map<String,Object>>> getAllFieldsById(String id, String tableName) {
                return null;
            }

            @Override
            public RespEntity<List<Map<String, Object>>> getAllDataWithDict(FieldDictMapVo dictMapVo) {
                return null;
            }

            @Override
            public RespEntity<List<FieldDictVo>> getDictData(DatasourceDictVo datasourceDictVo) {
                return null;
            }

            @Override
            public RespEntity<Integer> getDataCount(String id, String sql) {
                return null;
            }

			@Override
			public RespEntity<Integer> getDataCountView(String datasourceId, String countSql) {
				return null;
			}
        };
    }
}
