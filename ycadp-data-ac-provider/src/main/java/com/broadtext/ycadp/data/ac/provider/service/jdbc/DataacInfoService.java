/*
 * JDBCUtils.java
 * Created at 2019/6/25
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.service.jdbc;


import com.broadtext.ycadp.core.common.service.BaseService;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldInfoVo;

import java.util.List;
import java.util.Map;

/**
 * 数据源连接通用接口
 *
 * @author ouhaoliang
 */
public interface DataacInfoService extends BaseService<TBDatasourceConfig, String> {

    /**
     * 根据信息获取数据源下表名的接口
     *
     * @param tbDatasourceConfig
     * @return string类型的list集合
     */
    List<String> getAllTables(TBDatasourceConfig tbDatasourceConfig) throws Exception;

    /**
     * 根据信息获取所有该表下的数据,只允许在数据接入的查询中使用
     *
     * @param tbDatasourceConfig
     * @param sql
     * @return list集合
     */
    List getAllData(TBDatasourceConfig tbDatasourceConfig, String sql) throws Exception;

    /**
     * 根据信息获取所有该表下的数据(包括字典转换),建议在填写字典key的预览中使用
     *
     * @param datasourceId
     * @param sql
     * @param dictMap
     * @return
     */
    List<Map<String, Object>> getAllDataWithDict(String datasourceId, String sql, Map<String, List<FieldDictVo>> dictMap) throws Exception;

    /**
     * 根据信息获取所有该表下的数据(包括字典转换),建议在数据接入输入字典sql使用
     *
     * @param datasourceId
     * @param dictSql
     * @param key
     * @return
     */
    List<FieldDictVo> getDictData(String datasourceId, String dictSql, String key) throws Exception;

    /**
     * 获取数据数量
     *
     * @param tbDatasourceConfig
     * @param sql
     * @return Integer
     */
    Integer getDataCount(TBDatasourceConfig tbDatasourceConfig, String sql) throws Exception;

    /**
     * 获取数据数量(参数id)
     * @param id
     * @param sql
     * @return
     */
    Integer getDataCount(String id, String sql) throws Exception;

    /**
     * 获取数据接入表信息
     *
     * @param table
     * @return
     */
    List<FieldInfoVo> getAllFields(String datasourceId, String table) throws Exception;

    /**
     * 建表
     *
     * @param datasourceId
     * @param sql
     */
    void crateTable(String datasourceId, String sql) throws Exception;

    /**
     * 更新
     *
     * @param datasourceId
     * @param sql
     */
    void update(String datasourceId, String sql) throws Exception;

    /**
     * 插入
     *
     * @param datasourceId
     * @param sql
     */
    void insert(String datasourceId, String sql) throws Exception;

    /**
     * 查询某字段的返回数据
     *
     * @param datasourceId
     * @param sql
     * @return String
     */
    String query(String datasourceId, String sql) throws Exception;

    /**
     * 删除
     *
     * @param datasourceId
     * @param sql
     */
    void delete(String datasourceId, String sql) throws Exception;

    /**
     * @param datasourceId
     * @param sql
     * @throws Exception
     */
    void truncate(String datasourceId, String sql) throws Exception;

    /**
     * 测试连接是否成功
     *
     * @param tbDatasourceConfig
     * @return Map
     */
    Map<Boolean, String> check(TBDatasourceConfig tbDatasourceConfig) throws Exception;

    /**
     * 获取去重字段list
     * @param datasourceId
     * @param sql
     * @return
     */
    List<String> getDistinctFields(String datasourceId,String sql) throws Exception;

    /**
     *
     * @param sql
     * @param skipResults
     * @param maxResults
     * @return
     * @throws Exception
     */
    String getLimitString(String sql, int skipResults, int maxResults) throws Exception;
}
