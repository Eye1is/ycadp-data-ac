package com.broadtext.ycadp.data.ac.provider.controller;

import com.alibaba.fastjson.JSON;
import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.constants.DataSourceType;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceExcel;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.provider.service.DataExcelService;
import com.broadtext.ycadp.data.ac.provider.service.jdbc.DataacInfoService;
import com.broadtext.ycadp.data.ac.provider.service.DataacService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
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
    @Autowired
    private DataacInfoService mysql;
    @Autowired
    private DataacInfoService oracle;
    @Autowired
    private DataacInfoService postgresql;
    @Autowired
    private DataacService dataacService;
    @Autowired
    private DataExcelService excelService;

    /**
     * 查找数据库表列表
     * @param id 对象id
     * @param tableName 表名
     * @return  返回接口数据
     */
    @GetMapping("/data/datatable/{id}")
    public RespEntity searchTables(@PathVariable(value="id") String id,String tableName) {

        TBDatasourceConfig datasource=dataacService.findById(id);
        List<String> list=new ArrayList<String>();
        List<String> listContains=new ArrayList<String>();
        Map map =new HashMap();
        try {
            String datasourceType = dataacService.getFieldTypeById(id);
            switch (datasourceType) {
                case DataSourceType.MYSQL:
                    if (tableName == null) {//无筛选条件查询所有
                        list = mysql.getAllTables(datasource);
                        map.put("list", list);
                    } else if (!"".equals(tableName)) {//有筛选条件
                        list = mysql.getAllTables(datasource);
                        if (list.size() > 0) {
                            for (String str : list) {
                                if (str.contains(tableName)) {
                                    listContains.add(str);
                                }
                            }
                        }
                        map.put("list", listContains);
                    } else {//无筛选条件查询所有
                        list = mysql.getAllTables(datasource);
                        map.put("list", list);
                    }
                    break;
                case DataSourceType.ORACLE:
                    if (tableName == null) {//无筛选条件查询所有
                        list = oracle.getAllTables(datasource);
                        map.put("list", list);
                    } else if (!"".equals(tableName)) {//有筛选条件
                        list = oracle.getAllTables(datasource);
                        if (list.size() > 0) {
                            for (String str : list) {
                                if (str.contains(tableName)) {
                                    listContains.add(str);
                                }
                            }
                        }
                        map.put("list", listContains);
                    } else {//无筛选条件查询所有
                        list = oracle.getAllTables(datasource);
                        map.put("list", list);
                    }
                    break;
                case DataSourceType.PostgreSQL:
                    if (tableName == null) {//无筛选条件查询所有
                        list = postgresql.getAllTables(datasource);
                        map.put("list", list);
                    } else if (!"".equals(tableName)) {//有筛选条件
                        list = postgresql.getAllTables(datasource);
                        if (list.size() > 0) {
                            for (String str : list) {
                                if (str.contains(tableName)) {
                                    listContains.add(str);
                                }
                            }
                        }
                        map.put("list", listContains);
                    } else {//无筛选条件查询所有
                        list = postgresql.getAllTables(datasource);
                        map.put("list", list);
                    }
                    break;
                case DataSourceType.EXCEL:
                    List<TBDatasourceExcel> listByDataSourceId = excelService.getListByDataSourceId(id);
                    if (tableName == null) {//无筛选条件查询所有
//                        List<String> sheetTableNameList = new ArrayList<>();
//                        List<String> sheetNameList = new ArrayList<>();
                        for (TBDatasourceExcel e : listByDataSourceId) {
//                            sheetTableNameList.add(e.getSheetTableName());
                            list.add(e.getSheetTableName());
                        }
                        map.put("list", list);
                    } else if (!"".equals(tableName)) {//有筛选条件
                        for (TBDatasourceExcel e : listByDataSourceId) {
                            if (e.getSheetTableName().contains(tableName)) {
                                list.add(e.getSheetTableName());
                            }
                        }
                        map.put("list", list);
                    } else {//无筛选条件查询所有
                        for (TBDatasourceExcel e : listByDataSourceId) {
                            list.add(e.getSheetTableName());
                        }
                        map.put("list", list);
                    }
                    break;
                default:
                    break;
            }
            return new RespEntity(RespCode.SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }
    /**
     * 查询数据库表数据
     * @param id 对象id
     * @param request 请求request
     * @return 返回接口数据
     */
    @GetMapping("data/datatables/{id}")
    public RespEntity searchDataTable(HttpServletRequest request, @PathVariable(value="id") String id) throws Exception {
        TBDatasourceConfig datasource=dataacService.findById(id);
        String datasourceType = dataacService.getFieldTypeById(id);
        String ispage=request.getParameter("isPage");
        String tableName=request.getParameter("tableName");
//        List<Map<String, Object>> list=new ArrayList<Map<String, Object>>();
        String sql="";
        int count=0;
        Map map =new HashMap();
        if (ispage==null||"true".equals(ispage)){
            //分页
            String pageNum=request.getParameter("pageNum");
            String pageSize=request.getParameter("pageSize");
            sql="select * from "+tableName;
            int skipResults = Integer.parseInt(pageSize) * (Integer.parseInt(pageNum) - 1);
            int maxResults = Integer.parseInt(pageSize);
//          sql="select * from "+tableName+" limit " +Integer.parseInt(pageSize)*(Integer.parseInt(pageNum)-1)+","+Integer.parseInt(pageSize);
            switch (datasourceType) {
                case DataSourceType.MYSQL:
                    sql = mysql.getLimitString(sql, skipResults, maxResults);
                    break;
                case DataSourceType.ORACLE:
                    sql = oracle.getLimitString(sql, skipResults, maxResults);
                    break;
                case DataSourceType.PostgreSQL:
                    sql = postgresql.getLimitString(sql, skipResults, maxResults);
                    break;
                default:
                    break;
            }
        }else {
            //不分页
            sql="select * from "+tableName;
        }
        try {
            List<Map<String, Object>> allData = new ArrayList();
            String sqlTotal="select * from "+tableName;
            switch (datasourceType) {
                case DataSourceType.MYSQL:
                    allData = mysql.getAllData(datasource, sql);
                    count=mysql.getDataCount(datasource,sqlTotal);
                    break;
                case DataSourceType.ORACLE:
                    allData = oracle.getAllData(datasource, sql);
                    count = oracle.getDataCount(datasource, sqlTotal);
                    break;
                case DataSourceType.PostgreSQL:
                    allData = postgresql.getAllData(datasource, sql);
                    count = postgresql.getDataCount(datasource, sqlTotal);
                    break;
                default:
                    break;
            }
//            String str = JSON.toJSONString(allData);
            map.put("total",count);
            map.put("list",allData);
            return new RespEntity(RespCode.SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }
}
