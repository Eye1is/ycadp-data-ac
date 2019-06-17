package com.broadtext.ycadp.data.ac.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public abstract class AbstractDynamicDataSource<T extends DataSource>
        {
    /** 日志 */
    protected Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 创建数据源
     * @param driverClassName 数据库驱动名称
     * @param url 连接地址
     * @param username 用户名
     * @param password 密码
     * @return 数据源{@link T}
     */
    public abstract T createDataSource(String driverClassName, String url, String username,
                                       String password);

}
