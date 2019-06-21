package com.broadtext.ycadp.data.ac.api.utils;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.List;

public class DruidDynamicDataSource extends AbstractDynamicDataSource{
    private DruidDataSource ds;
    private DruidDynamicDataSource(){}
    private boolean testWhileIdle = true;
    //检测连接池中连接的可用性,这里建议配置为TRUE，防止取到的连接不可用
    private boolean testOnBorrow = true;
    private boolean testOnReturn = false;

    // 是否打开连接泄露自动检测
    private boolean removeAbandoned = true;
    // 连接长时间没有使用，被认为发生泄露时长
    private long removeAbandonedTimeoutMillis = 3000 * 100;
    // 发生泄露时是否需要输出 log，建议在开启连接泄露检测时开启，方便排错
    private boolean logAbandoned = true;

    // 只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true，使用oracle时可以设定此值。
    //    private int maxPoolPreparedStatementPerConnectionSize = -1;

    // 配置监控统计拦截的filters
    private String filters; // 监控统计："stat" 防SQL注入："wall" 组合使用： "stat,wall"
    private List<Filter> filterList;
    /**
     * 数据库连接池单例
     * @return dbPoolConnection
     */
    private static class DruidDynamicDataSourceInstance{
        private static final DruidDynamicDataSource INSTANCE = new DruidDynamicDataSource();
    }

    static DruidDynamicDataSource getInstance() {
        return DruidDynamicDataSourceInstance.INSTANCE;
    }

    /*
     * 创建数据源，这里创建的数据源是带有连接池属性的
     */
    @Override
    public DruidDataSource createDataSource(String driverClassName, String url, String username,
                                            String password) {
        ds = new DruidDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        //配置初始化大小、最小、最大
        ds.setInitialSize(1);
        ds.setMinIdle(1);
        ds.setMaxActive(20);
        //配置获取连接等待超时的时间
        ds.setMaxWait(60000);
        //配置间隔多久才进行一次检测,检测连接错误的连接,单位是毫秒
        ds.setTimeBetweenConnectErrorMillis(30000);
        //配置间隔多久才进行一次检测,检测需要关闭的空闲连接,单位是毫秒
        ds.setTimeBetweenEvictionRunsMillis(60000);
        //配置一个连接在池中最小生存的时间,单位是毫秒
        ds.setMinEvictableIdleTimeMillis(30000);
        //验证连接有效与否的SQL，不同的数据配置不同
        ds.setValidationQuery("select 1");
        ds.setTestWhileIdle(testWhileIdle);
        ds.setTestOnBorrow(testOnBorrow);
        ds.setTestOnReturn(testOnReturn);

        ds.setRemoveAbandoned(removeAbandoned);
        ds.setRemoveAbandonedTimeoutMillis(removeAbandonedTimeoutMillis);
        ds.setLogAbandoned(logAbandoned);
        //打开PSCache，并且指定每个连接上PSCache的大小
        // 只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true，参照druid的源码
        ds.setMaxPoolPreparedStatementPerConnectionSize(20);

        if (StringUtils.isNotBlank(filters))
            try {
                ds.setFilters(filters);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        addFilterList(ds);
        return ds;
    }
    /*
    获取数据库连接
     */
    public DruidPooledConnection getDataSourceConnection(String driverClassName, String url, String username,
                                                         String password) throws SQLException {
        DruidDynamicDataSource druidDynamicDataSource = DruidDynamicDataSource.getInstance();
        DruidDataSource dataSource = druidDynamicDataSource.createDataSource(driverClassName, url, username, password);
        return dataSource.getConnection();
    }

    private void addFilterList(DruidDataSource ds) {
        if (filterList != null) {
            List<Filter> targetList = ds.getProxyFilters();
            for (Filter add : filterList) {
                boolean found = false;
                for (Filter target : targetList) {
                    if (add.getClass().equals(target.getClass())) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    targetList.add(add);
            }
        }
    }

    public void close(){
        if (ds!=null) {
            this.ds.close();
        }
    }
}
