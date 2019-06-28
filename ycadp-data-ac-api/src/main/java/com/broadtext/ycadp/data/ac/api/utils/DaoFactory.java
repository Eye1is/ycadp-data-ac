/*
 * JDBCUtils.java
 * Created at 2019/6/25
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.utils;

import com.broadtext.ycadp.data.ac.api.constants.DataSourceType;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;

/**
 * 数据源连接工厂类
 * @author ouhaoliang
 */
public abstract class DaoFactory implements DataInfoInterface{
    /**
     * 根据数据库的不同,获得不同的DAO工厂
     * @param tbDatasourceConfig 数据源信息
     * @return 工厂连接对象
     * @throws Exception 异常类型为:不支持该类型数据源
     */
    public static DaoFactory getDaoFactory(TBDatasourceConfig tbDatasourceConfig) throws Exception {
        if (DataSourceType.MYSQL.equals(tbDatasourceConfig.getDatasourceType())) {
            return new DataInfoForMySQLImpl(tbDatasourceConfig);
        } else {
            throw new Exception("不支持该类型数据源");
        }
    }
}
