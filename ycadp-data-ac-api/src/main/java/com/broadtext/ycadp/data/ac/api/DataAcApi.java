package com.broadtext.ycadp.data.ac.api;

import com.broadtext.ycadp.base.entity.ListPager;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.hystrix.DataAcFallbackFactor;
import com.broadtext.ycadp.data.ac.api.vo.DataSourceListVo;
import com.broadtext.ycadp.data.ac.api.vo.TBDatasourceConfigVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author PC-Xuchenglong
 */
@FeignClient(name = "${dataacService}",fallbackFactory = DataAcFallbackFactor.class)
public interface DataAcApi {
    /**
     * 新增数据源
     * @param datasourceConfig
     * @return
     */
    @PostMapping("/data/datasource")
    RespEntity<Object> addDatasource(@RequestBody TBDatasourceConfigVo datasourceConfig);

    /**
     * 删除数据源
     * @param id
     * @return
     */
    @DeleteMapping("/data/datasource/{id}")
    RespEntity<Object> deleteDatasource(@PathVariable("id") String id);

    /**
     * 查询数据源列表
     * @param pager
     * @return
     */
    @GetMapping("/data/datasource")
    RespEntity<List<TBDatasourceConfig>> getDatasources(ListPager<DataSourceListVo> pager);

    /**
     * 查询数据源明细信息
     * @param id
     * @return
     */
    @GetMapping("/data/datasource/{id}")
    RespEntity<TBDatasourceConfig> getDatasource(@PathVariable("id") String id);

    /**
     * 编辑数据源
     * @param id
     * @param datasourceConfig
     * @return
     */
    @PutMapping("/data/datasource/{id}")
    RespEntity<TBDatasourceConfig> updateDatasource(@PathVariable("id") String id,@RequestBody TBDatasourceConfigVo datasourceConfig);

    /**
     * 查找数据库表列表
     * @param id 对象id
     * @param tableName 表名
     * @return  返回接口数据
     */
    @GetMapping("/data/datatable/{id}")
    RespEntity<Map> searchTables(@PathVariable(value="id") String id, String tableName);

    /**
     * 查询数据库表数据
     * @param id 对象id
     * @param tableName 表名
     * @return 返回接口数据
     */
    @GetMapping("data/datatables/{id}")
    RespEntity<Map> searchDataTable(HttpServletRequest request,@PathVariable(value="id") String id, String tableName);

    /**
     * 测试数据源连接
     * @param datasourceConfig 数据源对象
     * @return RespEntity
     */
    @PostMapping("/data/connecttest")
    RespEntity<String> connectDatasource(@RequestBody TBDatasourceConfig datasourceConfig);
}
