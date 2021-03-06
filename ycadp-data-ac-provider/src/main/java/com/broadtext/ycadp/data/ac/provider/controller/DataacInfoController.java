/*
 * DataacInfoController.java
 * Created at 2019/7/8
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.controller;

import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.constants.DataSourceType;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceExcel;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.api.vo.DatasourceDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictMapVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldInfoVo;
import com.broadtext.ycadp.data.ac.provider.service.DataExcelService;
import com.broadtext.ycadp.data.ac.provider.service.jdbc.DataacInfoService;
import com.broadtext.ycadp.data.ac.provider.service.DataacService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class DataacInfoController {
    /**
     * 服务层依赖注入
     */
    @Autowired
    private DataacInfoService mysql;
    @Autowired
    private DataacInfoService oracle;
    @Autowired
    private DataacInfoService postgresql;
    @Autowired
    private DataacInfoService db2;
    @Autowired
    private DataacInfoService excel;
    @Autowired
    private DataacInfoService sqlServer;
    @Autowired
    private DataacService dataacService;
    @Autowired
    private DataExcelService dataExcelService;

    @GetMapping("/data/test/{id}")
    public RespEntity test(@PathVariable(value = "id") String id) {
        RespEntity respEntity = null;
        try {
            TBDatasourceConfig datasourceConfig = dataacService.findById(id);
            if (datasourceConfig != null) {
                Integer allTables = sqlServer.getDataCount(datasourceConfig,"select * from spt_monitor");
                return new RespEntity(RespCode.SUCCESS,allTables);
            } else {
                respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }


    @GetMapping("/data/getType/{id}")
    public RespEntity getFieldTypeById(@PathVariable(value = "id") String id) {
        RespEntity respEntity = null;
        try {
            String datasourceType = dataacService.getFieldTypeById(id);
            if (datasourceType != null && !"".equals(datasourceType)) {
                return new RespEntity(RespCode.SUCCESS,datasourceType);
            } else {
                respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }

    /**
     * 根据数据源id和表名获取该表中的所有字段名
     * @param id
     * @param tableName
     * @return
     */
    @GetMapping("/data/datasourceInfo/{id}")
    public RespEntity getAllFieldsById(@PathVariable(value = "id") String id, String tableName) {
        RespEntity respEntity = null;
        List<FieldInfoVo> allFields = new ArrayList<>();
        try {
            String datasourceType = dataacService.getFieldTypeById(id);
            allFields = getFieldInfoVos(id, tableName, allFields, datasourceType);
            if (null != allFields && allFields.size() != 0) {
                respEntity = new RespEntity(RespCode.SUCCESS, allFields);
            } else {
                respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }

    @GetMapping("/data/datasourceDictDataBySql")
    public RespEntity getDictData(@RequestBody DatasourceDictVo datasourceDictVo) throws Exception {
        RespEntity respEntity;
        List<FieldDictVo> dictFields = new ArrayList<>();
        String datasourceId = datasourceDictVo.getDatasourceId();
        String dictSql = datasourceDictVo.getDictSql();
        String dictKey = datasourceDictVo.getDictKey();
        String datasourceType = dataacService.getFieldTypeById(datasourceDictVo.getDatasourceId());
        dictFields = getFieldDictVos(dictFields, datasourceId, dictSql, dictKey, datasourceType);
        if (null != dictFields && dictFields.size() != 0) {
            respEntity = new RespEntity(RespCode.SUCCESS, dictFields);
        } else {
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }

    @PostMapping("/data/datasourceDictDataByMap")
    public RespEntity getAllDataWithDict(@RequestBody FieldDictMapVo dictMapVo) throws Exception {
        RespEntity respEntity;
        List<Map<String, Object>> dictMapFields = new ArrayList<>();
        String datasourceId = dictMapVo.getDatasourceId();
        String sql = dictMapVo.getSql();
        Map<String, List<FieldDictVo>> dictMap = dictMapVo.getDictMap();
        String datasourceType = dataacService.getFieldTypeById(dictMapVo.getDatasourceId());
        datasourceType = StringUtils.isEmpty(datasourceType) ? DataSourceType.PostgreSQL : datasourceType;
        dictMapFields = getDictMapFields(dictMapFields, datasourceId, sql, dictMap, datasourceType);
        if (null != dictMapFields && dictMapFields.size() != 0) {
            respEntity = new RespEntity(RespCode.SUCCESS, dictMapFields);
        } else {
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }

    /**
     * @param id
     * @param sql
     * @return
     */
    @GetMapping("/data/datasourceDataCount/{id}")
    public RespEntity getDataCount(@PathVariable(value = "id") String id, String sql) throws Exception {
        RespEntity respEntity;
        String datasourceType = dataacService.getFieldTypeById(id);
        Integer dataCount = 0;
        dataCount = getCount(id, sql, datasourceType, dataCount);
        if (null == dataCount) {
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        } else if (dataCount == 0) {
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "无数据");
        } else {
            respEntity = new RespEntity(RespCode.SUCCESS, dataCount);
        }
        return respEntity;
    }

    /**
     * @param
     * @param
     * @return
     */
    @PostMapping("/data/datasourceDataCountView")
    public RespEntity getDataCountView(@RequestBody LinkedMultiValueMap<String, String> countMultiValue) throws Exception {
        RespEntity respEntity;
        List<String> strings = countMultiValue.get("data");
        String datasourceId = strings.get(0);
        String countSql = strings.get(1);
        String datasourceType = dataacService.getFieldTypeById(datasourceId);
        Integer dataCount = 0;
        datasourceType = StringUtils.isEmpty(datasourceType) ? DataSourceType.PostgreSQL : datasourceType;
        dataCount = getCount(datasourceId, countSql, datasourceType, dataCount);
        if (null == dataCount) {
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE, 0);
        } else {
            respEntity = new RespEntity(RespCode.SUCCESS, dataCount);
        }
        return respEntity;
    }

    /**
     * 获取某个字段去重之后的list
     *
     * @param datasourceId
     * @param sql
     * @return
     */
    @GetMapping("/data/distinctFields")
    public RespEntity getDistinctFields(String datasourceId, String sql) throws Exception {
        List<String> distinctFields = new ArrayList<>();
        String datasourceType = dataacService.getFieldTypeById(datasourceId);
        distinctFields = getDistinctFields(datasourceId, sql, distinctFields, datasourceType);
        return new RespEntity(RespCode.SUCCESS, distinctFields);
    }

    /**
     * 根据数据源id和sheet名称获取excel映射关系实体
     * @param id
     * @param sheetName
     * @return
     */
    @GetMapping("/data/excel")
    public RespEntity getExcelMappingEntity(String id, String sheetName) {
        TBDatasourceExcel byIdAndSheetName = dataExcelService.findByIdAndSheetName(id, sheetName);
        if (byIdAndSheetName != null) {
            return new RespEntity(RespCode.SUCCESS, byIdAndSheetName);
        } else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }

    private List<FieldInfoVo> getFieldInfoVos(String id, String tableName, List<FieldInfoVo> allFields, String datasourceType) throws Exception {
        switch (datasourceType) {
            case DataSourceType.MYSQL:
                allFields = mysql.getAllFields(id, tableName);
                break;
            case DataSourceType.ORACLE:
                allFields = oracle.getAllFields(id, tableName);
                break;
            case DataSourceType.DB2:
                allFields = db2.getAllFields(id, tableName);
                break;
            case DataSourceType.PostgreSQL:
                allFields = postgresql.getAllFields(id, tableName);
                break;
            case DataSourceType.SQLServer:
                allFields = sqlServer.getAllFields(id, tableName);
                break;
            case DataSourceType.EXCEL:
//                    //先根据sheetName获取sheetTableName
//                    TBDatasourceExcel byIdAndSheetName = dataExcelService.findByIdAndSheetName(id, tableName);
//                    String sheetTableName = byIdAndSheetName.getSheetTableName();
                allFields = excel.getAllFields(id, tableName);
                break;
            default:
                break;
        }
        return allFields;
    }

    private List<FieldDictVo> getFieldDictVos(List<FieldDictVo> dictFields, String datasourceId, String dictSql, String dictKey, String datasourceType) throws Exception {
        switch (datasourceType) {
            case DataSourceType.MYSQL:
                dictFields = mysql.getDictData(datasourceId, dictSql, dictKey);
                break;
            case DataSourceType.ORACLE:
                dictFields = oracle.getDictData(datasourceId, dictSql, dictKey);
                break;
            case DataSourceType.PostgreSQL:
                dictFields = postgresql.getDictData(datasourceId, dictSql, dictKey);
                break;
            case DataSourceType.DB2:
                dictFields = db2.getDictData(datasourceId, dictSql, dictKey);
                break;
            case DataSourceType.SQLServer:
                dictFields = sqlServer.getDictData(datasourceId, dictSql, dictKey);
                break;
            case DataSourceType.EXCEL:
                dictFields = excel.getDictData(datasourceId, dictSql, dictKey);
                break;
            default:
                break;
        }
        return dictFields;
    }

    private List<Map<String, Object>> getDictMapFields(List<Map<String, Object>> dictMapFields, String datasourceId, String sql, Map<String, List<FieldDictVo>> dictMap, String datasourceType) throws Exception {
        switch (datasourceType) {
            case DataSourceType.MYSQL:
                dictMapFields = mysql.getAllDataWithDict(datasourceId, sql, dictMap);
                break;
            case DataSourceType.ORACLE:
                dictMapFields = oracle.getAllDataWithDict(datasourceId, sql, dictMap);
                break;
            case DataSourceType.DB2:
                dictMapFields = db2.getAllDataWithDict(datasourceId, sql, dictMap);
                break;
            case DataSourceType.PostgreSQL:
                dictMapFields = postgresql.getAllDataWithDict(datasourceId, sql, dictMap);
                break;
            case DataSourceType.SQLServer:
                dictMapFields = sqlServer.getAllDataWithDict(datasourceId, sql, dictMap);
                break;
            case DataSourceType.EXCEL:
                dictMapFields = excel.getAllDataWithDict(datasourceId, sql, dictMap);
                break;
            default:
                break;
        }
        return dictMapFields;
    }

    private Integer getCount(String datasourceId, String countSql, String datasourceType, Integer dataCount) throws Exception {
        switch (datasourceType) {
            case DataSourceType.MYSQL:
                dataCount = mysql.getDataCount(datasourceId, countSql);
                break;
            case DataSourceType.ORACLE:
                dataCount = oracle.getDataCount(datasourceId, countSql);
                break;
            case DataSourceType.DB2:
                dataCount = db2.getDataCount(datasourceId, countSql);
                break;
            case DataSourceType.PostgreSQL:
                dataCount = postgresql.getDataCount(datasourceId, countSql);
                break;
            case DataSourceType.SQLServer:
                dataCount = sqlServer.getDataCount(datasourceId, countSql);
                break;
            case DataSourceType.EXCEL:
                dataCount = excel.getDataCount(datasourceId, countSql);
            default:
                break;
        }
        return dataCount;
    }

    private List<String> getDistinctFields(String datasourceId, String sql, List<String> distinctFields, String datasourceType) throws Exception {
        switch (datasourceType) {
            case DataSourceType.MYSQL:
                distinctFields = mysql.getDistinctFields(datasourceId, sql);
                break;
            case DataSourceType.ORACLE:
                distinctFields = oracle.getDistinctFields(datasourceId, sql);
                break;
            case DataSourceType.DB2:
                distinctFields = db2.getDistinctFields(datasourceId, sql);
                break;
            case DataSourceType.SQLServer:
                distinctFields = sqlServer.getDistinctFields(datasourceId, sql);
                break;
            case DataSourceType.PostgreSQL:
                distinctFields = postgresql.getDistinctFields(datasourceId, sql);
                break;
            default:
                break;
        }
        return distinctFields;
    }
}
