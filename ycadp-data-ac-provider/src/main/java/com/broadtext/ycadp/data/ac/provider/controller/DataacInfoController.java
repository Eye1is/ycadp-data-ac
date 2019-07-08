/*
 * DataacInfoController.java
 * Created at 2019/7/8
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.controller;

import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldInfoVo;
import com.broadtext.ycadp.data.ac.provider.service.DataacInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class DataacInfoController {
    /**服务层依赖注入*/
    @Autowired
    private DataacInfoService dataacInfoService;

    /**
     *
     * @param id
     * @param tableName
     * @return
     */
    @GetMapping("/data/datasourceInfo/{id}")
    public RespEntity getAllFieldsById(@PathVariable(value="id") String id, String tableName){
        RespEntity respEntity;
        List<FieldInfoVo> allFields = dataacInfoService.getAllFields(id, tableName);
        if (null != allFields && allFields.size() !=0 ){
            respEntity=new RespEntity(RespCode.SUCCESS,allFields);
        }else {
            respEntity=new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }

    @GetMapping("/data/datasourceDictData/{id}")
    public RespEntity getDictData(@PathVariable(value="id")String datasourceId, String dictSql, String dictKey){
        RespEntity respEntity;
        List<FieldDictVo> dictFields = dataacInfoService.getDictData(datasourceId, dictSql,dictKey);
        if (null != dictFields && dictFields.size() !=0 ){
            respEntity=new RespEntity(RespCode.SUCCESS,dictFields);
        }else {
            respEntity=new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }

    @GetMapping("/data/datasourceDictDataByMap/{id}")
    public RespEntity getAllDataWithDict(@PathVariable(value="id")String datasourceId, String sql, Map<String, List<FieldDictVo>> dictMap){
        RespEntity respEntity;
        List<Map<String, Object>> dictMapFields = dataacInfoService.getAllDataWithDict(datasourceId, sql,dictMap);
        if (null != dictMapFields && dictMapFields.size() !=0 ){
            respEntity=new RespEntity(RespCode.SUCCESS,dictMapFields);
        }else {
            respEntity=new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }
}
