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
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.api.vo.DatasourceDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictMapVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldInfoVo;
import com.broadtext.ycadp.data.ac.provider.service.DataacInfoService;
import com.broadtext.ycadp.data.ac.provider.service.DataacService;
import lombok.extern.slf4j.Slf4j;
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
    private DataacService dataacService;

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
            if (DataSourceType.MYSQL.equals(datasourceType)) {
                allFields = mysql.getAllFields(id, tableName);
            } else if (DataSourceType.ORACLE.equals(datasourceType)) {
                allFields = oracle.getAllFields(id, tableName);
            }
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
    public RespEntity getDictData(@RequestBody DatasourceDictVo datasourceDictVo) {
        RespEntity respEntity;
        List<FieldDictVo> dictFields = new ArrayList<>();
        String datasourceId = datasourceDictVo.getDatasourceId();
        String dictSql = datasourceDictVo.getDictSql();
        String dictKey = datasourceDictVo.getDictKey();
        String datasourceType = dataacService.getFieldTypeById(datasourceDictVo.getDatasourceId());
        if (DataSourceType.MYSQL.equals(datasourceType)) {
            dictFields = mysql.getDictData(datasourceId, dictSql, dictKey);
        } else if (DataSourceType.ORACLE.equals(datasourceType)) {
            dictFields = oracle.getDictData(datasourceId, dictSql, dictKey);
        }
        if (null != dictFields && dictFields.size() != 0) {
            respEntity = new RespEntity(RespCode.SUCCESS, dictFields);
        } else {
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }

    @PostMapping("/data/datasourceDictDataByMap")
    public RespEntity getAllDataWithDict(@RequestBody FieldDictMapVo dictMapVo) {
        RespEntity respEntity;
        List<Map<String, Object>> dictMapFields = new ArrayList<>();
        String datasourceId = dictMapVo.getDatasourceId();
        String sql = dictMapVo.getSql();
        Map<String, List<FieldDictVo>> dictMap = dictMapVo.getDictMap();
        String datasourceType = dataacService.getFieldTypeById(dictMapVo.getDatasourceId());
        if (DataSourceType.MYSQL.equals(datasourceType)) {
            dictMapFields = mysql.getAllDataWithDict(datasourceId, sql, dictMap);
        } else if (DataSourceType.ORACLE.equals(datasourceType)) {
            dictMapFields = oracle.getAllDataWithDict(datasourceId, sql, dictMap);
        }
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
    public RespEntity getDataCount(@PathVariable(value = "id") String id, String sql) {
        RespEntity respEntity;
        String datasourceType = dataacService.getFieldTypeById(id);
        Integer dataCount = 0;
        if (DataSourceType.MYSQL.equals(datasourceType)) {
            dataCount = mysql.getDataCount(id, sql);
        } else if (DataSourceType.ORACLE.equals(datasourceType)) {
            dataCount = oracle.getDataCount(id, sql);
        }
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
    public RespEntity getDataCountView(@RequestBody LinkedMultiValueMap<String, String> countMultiValue) {
        RespEntity respEntity;
        List<String> strings = countMultiValue.get("data");
        String datasourceId = strings.get(0);
        String countSql = strings.get(1);
        String datasourceType = dataacService.getFieldTypeById(datasourceId);
        Integer dataCount = 0;
        if (DataSourceType.MYSQL.equals(datasourceType)) {
            dataCount = mysql.getDataCount(datasourceId, countSql);
        } else if (DataSourceType.ORACLE.equals(datasourceType)) {
            dataCount = oracle.getDataCount(datasourceId, countSql);
        }
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
    public RespEntity getDistinctFields(String datasourceId, String sql) {
        List<String> distinctFields = new ArrayList<>();
        String datasourceType = dataacService.getFieldTypeById(datasourceId);
        if (DataSourceType.MYSQL.equals(datasourceType)) {
            distinctFields = mysql.getDistinctFields(datasourceId, sql);
        } else if (DataSourceType.ORACLE.equals(datasourceType)) {
            distinctFields = oracle.getDistinctFields(datasourceId, sql);
        }
        return new RespEntity(RespCode.SUCCESS, distinctFields);
    }
}
