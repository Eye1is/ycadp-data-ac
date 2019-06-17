package com.broadtext.ycadp.data.ac.api.utils;

import com.broadtext.ycadp.data.ac.api.constants.DataSourceType;
import com.broadtext.ycadp.data.ac.api.vo.TBDatasourceConfigVo;

public abstract class DaoFactory implements DataInfoInterface{
    /**
     * 根据数据库的不同,获得不同的DAO工厂
     *
     * @param dsConfigVo 数据源信息
     * @return 工厂连接对象
     */
    public static DaoFactory getDaoFactory(TBDatasourceConfigVo dsConfigVo) {
        if (DataSourceType.DATASOURCE_TYPE_IS_MYSQL.equals(dsConfigVo.getDatasourceType())) {
            return new DataInfoForMySQLImpl(dsConfigVo);
        } else {
            return null;
        }
    }
}
