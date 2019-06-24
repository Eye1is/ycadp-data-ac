package com.broadtext.ycadp.data.ac.api.utils;

import com.broadtext.ycadp.data.ac.api.constants.DataSourceType;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;

public abstract class DaoFactory implements DataInfoInterface{
    /**
     * 根据数据库的不同,获得不同的DAO工厂
     *
     * @param tbDatasourceConfig 数据源信息
     * @return 工厂连接对象
     */
    public static DaoFactory getDaoFactory(TBDatasourceConfig tbDatasourceConfig) {
        if (DataSourceType.MYSQL.equals(tbDatasourceConfig.getDatasourceType())) {
            return new DataInfoForMySQLImpl(tbDatasourceConfig);
        } else {
            return null;
        }
    }
}
