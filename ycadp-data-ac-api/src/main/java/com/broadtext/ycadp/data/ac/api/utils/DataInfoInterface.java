/*
 * JDBCUtils.java
 * Created at 2019/6/25
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.utils;


import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;

import java.util.List;
import java.util.Map;

/**
 * 数据源连接通用接口
 * @author ouhaoliang
 */
public interface DataInfoInterface {

    /**
     * 根据信息获取数据源下表名的接口
     * @param tbDatasourceConfig
     * @return  string类型的list集合
     */
    public List<String> getAllTables(TBDatasourceConfig tbDatasourceConfig);

    /**
     * 根据信息获取所有该表下的数据
     * @param tbDatasourceConfig
     * @param sql
     * @return list集合
     */
    public List getAllData(TBDatasourceConfig tbDatasourceConfig, String sql);

    /**
     * 获取数据数量
     * @param tbDatasourceConfig
     * @param sql
     * @return Integer
     */
    public Integer getDataCount(TBDatasourceConfig tbDatasourceConfig,String sql);

    /**
     * 建表
     * @param tbDatasourceConfig
     * @param sql
     */
    public void crateTable(TBDatasourceConfig tbDatasourceConfig,String sql);

    /**
     * 更新
     * @param tbDatasourceConfig
     * @param sql
     */
    public void update(TBDatasourceConfig tbDatasourceConfig,String sql);

    /**
     * 插入
     * @param tbDatasourceConfig
     * @param sql
     */
    public void insert(TBDatasourceConfig tbDatasourceConfig,String sql);

    /**
     * 查询某字段的返回数据
     * @param tbDatasourceConfig
     * @param sql
     * @return String
     */
    public String query(TBDatasourceConfig tbDatasourceConfig,String sql);

    /**
     * 删除
     * @param tbDatasourceConfig
     * @param sql
     * @return String
     */
    public String delete(TBDatasourceConfig tbDatasourceConfig,String sql);

    /**
     * 测试连接是否成功
     * @param tbDatasourceConfig
     * @return Map
     */
    public Map<Boolean,String> check(TBDatasourceConfig tbDatasourceConfig);
}
