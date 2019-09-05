/*
 * DataacInfoController.java
 * Created at 2019/7/8
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.controller;

import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.api.vo.CountVo;
import com.broadtext.ycadp.data.ac.api.vo.DatasourceDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictMapVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldInfoVo;
import com.broadtext.ycadp.data.ac.provider.service.DataacInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class DataacInfoController {
    /**
     * 服务层依赖注入
     */
    @Autowired
    private DataacInfoService dataacInfoService;

    /**
     * @param id
     * @param tableName
     * @return
     */
    @GetMapping("/data/datasourceInfo/{id}")
    public RespEntity getAllFieldsById(@PathVariable(value = "id") String id, String tableName) {
        RespEntity respEntity;
        List<FieldInfoVo> allFields = dataacInfoService.getAllFields(id, tableName);
        if (null != allFields && allFields.size() != 0) {
            respEntity = new RespEntity(RespCode.SUCCESS, allFields);
        } else {
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }

    @GetMapping("/data/datasourceDictDataBySql")
    public RespEntity getDictData(@RequestBody DatasourceDictVo datasourceDictVo) {
        RespEntity respEntity;
        String datasourceId = datasourceDictVo.getDatasourceId();
        String dictSql = datasourceDictVo.getDictSql();
        String dictKey = datasourceDictVo.getDictKey();
        List<FieldDictVo> dictFields = dataacInfoService.getDictData(datasourceId, dictSql, dictKey);
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
        String datasourceId = dictMapVo.getDatasourceId();
        String sql = dictMapVo.getSql();
        Map<String, List<FieldDictVo>> dictMap = dictMapVo.getDictMap();
        List<Map<String, Object>> dictMapFields = dataacInfoService.getAllDataWithDict(datasourceId, sql, dictMap);
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
        Integer dataCount = dataacInfoService.getDataCount(id, sql);
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
    @GetMapping("/data/datasourceDataCountView")
    public RespEntity getDataCountView(@RequestBody CountVo countVo) {
        RespEntity respEntity;
        String datasourceId = countVo.getDatasourceId();
        String countSql = countVo.getCountSql();
        Integer dataCount = dataacInfoService.getDataCount(datasourceId, countSql);
        if (null == dataCount) {
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE,0);
        }else {
            respEntity = new RespEntity(RespCode.SUCCESS, dataCount);
        }
        return respEntity;
    }

    /**
     * 获取某个字段去重之后的list
     * @param datasourceId
     * @param sql
     * @return
     */
    @GetMapping("/data/distinctFields")
    public RespEntity getDistinctFields(String datasourceId,String sql){
        List<String> distinctFields = dataacInfoService.getDistinctFields(datasourceId, sql);
        return new RespEntity(RespCode.SUCCESS,distinctFields);
    }
}
