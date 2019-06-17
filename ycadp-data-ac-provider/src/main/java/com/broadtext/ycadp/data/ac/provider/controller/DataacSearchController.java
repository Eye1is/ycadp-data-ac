package com.broadtext.ycadp.data.ac.provider.controller;

import com.alibaba.fastjson.JSON;
import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.api.utils.DataInfoForMySQLImpl;
import com.broadtext.ycadp.data.ac.provider.service.DataacService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据库表查询Controller
 *
 * @author liuguangxuan
 */
@RestController
@Slf4j
public class DataacSearchController {
    /**服务层依赖注入*/
    private DataacService dataacService;

    /**
     * 查找数据库表列表
     * @param id
     * @return
     */
    @GetMapping("data/datatable")
    public RespEntity searchTables(@PathVariable(value="id") String id) {
        TBDatasourceConfig datasource=dataacService.findById(id);
        List<String> list=new ArrayList<String>();
        try {
            list= DataInfoForMySQLImpl.getDaoFactory(datasource).getAllTables(datasource);
            //list=getResultTable(datasource);
            return new RespEntity(RespCode.SUCCESS,list);
        } catch (Exception e) {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }

    }
    /**
     * 查询数据库表数据
     * @param id
     * @param tableName
     * @return
     */
    @GetMapping("data/datatables")
    public RespEntity searchDataTable(@PathVariable(value="id") String id,@PathVariable(value="tableName") String tableName) {
        TBDatasourceConfig datasource=dataacService.findById(id);
        List<Map<String, Object>> list=new ArrayList<Map<String, Object>>();
        String sql="select * from "+tableName;
        try {
            list=DataInfoForMySQLImpl.getDaoFactory(datasource).getAllData(datasource,sql);
            String str = JSON.toJSONString(list);
            return new RespEntity(RespCode.SUCCESS,str);
        } catch (Exception e) {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }
}
