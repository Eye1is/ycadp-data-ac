package com.broadtext.ycadp.data.ac.api;

import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceExcel;
import com.broadtext.ycadp.data.ac.api.hystrix.DataAcFallbackFactor;
import com.broadtext.ycadp.data.ac.api.vo.DatasourceDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictMapVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictVo;
import com.broadtext.ycadp.data.ac.api.vo.TBDatasourceConfigVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author PC-Xuchenglong
 */
@FeignClient(name = "dataac-service-001", fallbackFactory = DataAcFallbackFactor.class)
public interface DataAcApi {
    /**
     * 新增数据源
     *
     * @param datasourceConfig
     * @return
     */
    @PostMapping("/data/datasource")
    RespEntity<Object> addDatasource(@RequestBody TBDatasourceConfigVo datasourceConfig);

    /**
     * 删除数据源
     *
     * @param id
     * @return
     */
    @DeleteMapping("/data/datasource/{id}")
    RespEntity<Object> deleteDatasource(@PathVariable("id") String id);

    /**
     * 查询数据源列表
     *
     * @param
     * @return
     */
    @GetMapping("/data/datasource")
    RespEntity getDatasources(@RequestParam(value = "pageNum") String pageNum, @RequestParam(value = "pageSize") String pageSize);

    /**
     * 查询数据源明细信息
     *
     * @param id
     * @return
     */
    @GetMapping("/data/datasource/{id}")
    RespEntity<TBDatasourceConfig> getDatasource(@PathVariable("id") String id);

    /**
     * 编辑数据源
     *
     * @param id
     * @param datasourceConfig
     * @return
     */
    @PutMapping("/data/datasource/{id}")
    RespEntity<TBDatasourceConfig> updateDatasource(@PathVariable("id") String id, @RequestBody TBDatasourceConfigVo datasourceConfig);

    /**
     * 查找数据库表列表
     *
     * @param id        对象id
     * @param tableName 表名
     * @return 返回接口数据
     */
    @GetMapping("/data/datatable/{id}")
    RespEntity<Map> searchTables(@PathVariable(value = "id") String id, @RequestParam(value = "tableName") String tableName);

    /**
     * 查询数据库表数据
     *
     * @param id        对象id
     * @param tableName 表名
     * @return 返回接口数据
     */
    @GetMapping("data/datatables/{id}")
    RespEntity<Map> searchDataTable(@PathVariable(value = "id") String id, @RequestParam(value = "tableName") String tableName, @RequestParam(value = "pageNum") String pageNum, @RequestParam(value = "pageSize") String pageSize);

    /**
     * 测试数据源连接
     *
     * @param datasourceConfig 数据源对象
     * @return RespEntity
     */
    @PostMapping("/data/connecttest")
    RespEntity<String> connectDatasource(@RequestBody TBDatasourceConfig datasourceConfig);

    /**
     * 获取数据接入表信息
     *
     * @param id        datasourceId
     * @param tableName 表名
     * @return
     */
    @GetMapping("/data/datasourceInfo/{id}")
    RespEntity<List<Map<String,Object>>> getAllFieldsById(@PathVariable(value = "id") String id, @RequestParam(value = "tableName") String tableName);

    /**
     * 根据信息获取所有该表下的数据(包括字典转换),建议在填写字典key的预览中使用
     * @param dictMapVo
     * @return
     */
    @PostMapping("/data/datasourceDictDataByMap")
    RespEntity<List<Map<String, Object>>> getAllDataWithDict(@RequestBody FieldDictMapVo dictMapVo);

    /**
     * 根据信息获取所有该表下的字典数据,建议在数据接入输入字典sql使用
     * @param datasourceDictVo
     * @return
     */
    @GetMapping("/data/datasourceDictDataBySql")
    RespEntity<List<FieldDictVo>> getDictData(@RequestBody DatasourceDictVo datasourceDictVo);

    /**
     * 获取数据源某张表数据的数量(参数id)
     * @param id 数据源id
     * @param sql
     * @return
     */
    @GetMapping("/data/datasourceDataCount/{datasourceId}")
    RespEntity<Integer> getDataCount(@PathVariable(value="datasourceId") String id, @RequestParam(value="countSql") String sql);

    /**
     * 根据sql获取数据量
     * @param countVo
     * @return
     */
    @PostMapping("/data/datasourceDataCountView")
    RespEntity<Integer> getDataCountView(@RequestBody LinkedMultiValueMap<String,String> countMultiValue);

    /**
     * 获取某个字段的distinct列表
     * @param datasourceId
     * @param sql
     * @return
     */
    @GetMapping("/data/distinctFields")
    RespEntity<List<String>> getDistinctFields(@RequestParam(value="datasourceId") String datasourceId,@RequestParam(value="sql") String sql);

    /**
     * 根据数据源id和sheet名称获取excel映射关系实体
     * @param id
     * @param sheetName
     * @return
     */
    @GetMapping("/data/excel")
    RespEntity<TBDatasourceExcel> getExcelMappingEntity(@RequestParam(value = "id") String id, @RequestParam(value = "sheetName") String sheetName);
}
