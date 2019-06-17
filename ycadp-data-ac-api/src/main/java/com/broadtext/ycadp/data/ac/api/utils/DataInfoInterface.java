package com.broadtext.ycadp.data.ac.api.utils;

import com.broadtext.ycadp.data.ac.api.vo.TBDatasourceConfigVo;

import java.util.List;
import java.util.Map;

public interface DataInfoInterface {

    //根据信息获取数据源下表名的接口
    public List<String> getAllTables(TBDatasourceConfigVo dsConfigVo);

    //根据信息获取所有该表下的数据
    public List<Map<String, Object>> getAllData(TBDatasourceConfigVo dsConfigVo, String sql);

    //获取数据数量
    public Integer getDataCount(TBDatasourceConfigVo dsConfigVo,String sql);

    //建表
    public void crateTable(TBDatasourceConfigVo dsConfigVo,String sql);

    //更新
    public void update(TBDatasourceConfigVo dsConfigVo,String sql);

    //插入
    public void insert(TBDatasourceConfigVo dsConfigVo,String sql);

    //查询某字段的返回数据
    public String query(TBDatasourceConfigVo dsConfigVo,String sql);

    //删除
    public String delete(TBDatasourceConfigVo dsConfigVo,String sql);
}
