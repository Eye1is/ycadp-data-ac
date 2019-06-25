package com.broadtext.ycadp.data.ac.api.utils;


import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;

import java.util.List;
import java.util.Map;

public interface DataInfoInterface {

    //根据信息获取数据源下表名的接口
    public List<String> getAllTables(TBDatasourceConfig tbDatasourceConfig);

    //根据信息获取所有该表下的数据
    public List getAllData(TBDatasourceConfig tbDatasourceConfig, String sql);

    //获取数据数量
    public Integer getDataCount(TBDatasourceConfig tbDatasourceConfig,String sql);

    //建表
    public void crateTable(TBDatasourceConfig tbDatasourceConfig,String sql);

    //更新
    public void update(TBDatasourceConfig tbDatasourceConfig,String sql);

    //插入
    public void insert(TBDatasourceConfig tbDatasourceConfig,String sql);

    //查询某字段的返回数据
    public String query(TBDatasourceConfig tbDatasourceConfig,String sql);

    //删除
    public String delete(TBDatasourceConfig tbDatasourceConfig,String sql);

    //测试连接是否成功
    public Map<Boolean,String> check(TBDatasourceConfig tbDatasourceConfig);
}
