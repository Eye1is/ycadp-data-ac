/*
 * JDBCUtils.java
 * Created at 2019/6/25
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.broadtext.ycadp.data.ac.api.constants.DataSourceType;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 连接池工具类
 * @author ouhaoliang
 */
public class JDBCUtils {
    /**
     * 连接池中连接最大数量
     */
    private final Integer maxActive = 500;
    /**
     * 配置从连接池获取连接等待超时的时间
     */
    private final Integer maxWait = 10000;
    /**
     * 配置间隔多久启动一次DestroyThread
     */
    private final Integer timeBtnEviRuns = 600000;
    /**
     * 一个连接在池中最小生存的时间
     */
    private final Integer minEviIdleTime = 300000;
    /**
     * removeAbandoned功能超时时间
     */
    private final Integer rmAbdTimeout = 80;
    /**
     * 打开PSCache,并且指定每个连接上PSCache的大小
     */
    private final Integer maxPoolPsPerConnSize = 20;

    public JDBCUtils(TBDatasourceConfig tbDatasourceConfig){
        String decrypt = "";
        if(tbDatasourceConfig.getId() != null && !"".equals(tbDatasourceConfig.getId())){
            decrypt = AesUtil.decrypt(tbDatasourceConfig.getDatasourcePasswd(), "aaaaaaaaaaaaaaaaaaaaaaaa");
        }else{
            decrypt = tbDatasourceConfig.getDatasourcePasswd();
        }
//        String decrypt = AesUtil.decrypt(tbDatasourceConfig.getDatasourcePasswd(), "broadtext");
        setUrl(tbDatasourceConfig, decrypt);
        //    private final String driverClass = "com.mysql.cj.jdbc.Driver"; 默认驱动url可辨别
        datasourceInner.setUser(tbDatasourceConfig.getDatasourceUserName());//用户名

        //配置初始化大小、最小、最大
//        datasourceInner.setInitialSize(30);
//        datasourceInner.setMaxActive(maxActive);
//        datasourceInner.setMinIdle(0);
//        datasourceInner.setMaxWait(maxWait);
//        //设置从连接池获取连接时是否检查连接有效性,true时,每次都检查;false时,不检查
//        datasourceInner.setTestOnBorrow(false);
//        //设置从连接池获取连接时是否检查连接有效性,
//        //true时,如果连接空闲时间超过minEvictableIdleTimeMillis进行检查,否则不检查;false时,不检查
//        datasourceInner.setTestWhileIdle(true);
//        //设置往连接池归还连接时是否检查连接有效性,true时,每次都检查;false时,不检查
//        datasourceInner.setTestOnReturn(false);
//        datasourceInner.setPoolPreparedStatements(false);
//        //配置间隔多久启动一次DestroyThread,对连接池内的连接才进行一次检测,单位是毫秒
//        //检测时:1.如果连接空闲并且超过minIdle以外的连接,
//        //如果空闲时间超过minEvictableIdleTimeMillis设置的值则直接物理关闭。2.在minIdle以内的不处理
//        datasourceInner.setTimeBetweenEvictionRunsMillis(timeBtnEviRuns);
//        //配置一个连接在池中最小生存的时间,单位是毫秒
//        datasourceInner.setMinEvictableIdleTimeMillis(minEviIdleTime);
//        //连接泄露检查,打开removeAbandoned功能,
//        //连接从连接池借出后,长时间不归还,将触发强制回连接.
//        //回收周期随timeBetweenEvictionRunsMillis进行,如果连接为从连接池借出状态,并且未执行任何sql,
//        //并且从借出时间起已超过removeAbandonedTimeout时间,则强制归还连接到连接池中
//        datasourceInner.setRemoveAbandoned(true);
//        //超时时间,秒
//        datasourceInner.setRemoveAbandonedTimeout(rmAbdTimeout);
//        //关闭abandoned连接时输出错误日志,这样出现连接泄露时可以通过错误日志定位忘记关闭连接的位置
//        datasourceInner.setLogAbandoned(true);
//        //打开PSCache,并且指定每个连接上PSCache的大小
//        //只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true,参照druid的源码
//        datasourceInner.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPsPerConnSize);
        datasourceInner.setNumHelperThreads(10);
        datasourceInner.setMaxIdleTime(60);
        datasourceInner.setAcquireRetryAttempts(3);
        datasourceInner.setAcquireRetryDelay(300);
        datasourceInner.setCheckoutTimeout(3000);
        datasourceInner.setTestConnectionOnCheckin(true);
        datasourceInner.setUnreturnedConnectionTimeout(15);
        datasourceInner.setAcquireIncrement(5);
    }

    private void setUrl(TBDatasourceConfig tbDatasourceConfig, String decrypt) {
        switch(tbDatasourceConfig.getDatasourceType()){
            case DataSourceType.MYSQL:
                datasourceInner.setJdbcUrl("jdbc:mysql://" + tbDatasourceConfig.getConnectionIp()
                        + ":" + tbDatasourceConfig.getConnectionPort() + "/"
                        + tbDatasourceConfig.getSchemaDesc()
                        + "?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai");//url
                //验证连接有效与否的SQL,不同的数据配置不同
                //检验连接是否有效的查询语句。如果数据库Driver支持ping()方法,
                //则优先使用ping()方法进行检查,否则使用validationQuery查询进行检查。(Oracle jdbc Driver目前不支持ping方法)
//                datasourceInner.setValidationQuery("select 1");
                datasourceInner.setPassword(decrypt);//密码
                break;
            case DataSourceType.ORACLE:
                datasourceInner.setJdbcUrl("jdbc:oracle:thin:@" + tbDatasourceConfig.getConnectionIp()
                        + ":" + tbDatasourceConfig.getConnectionPort() + "/"
                        + tbDatasourceConfig.getSchemaDesc());//url
                //Oracle的验证语句
//                datasourceInner.setValidationQuery("select 1 FROM DUAL");
                datasourceInner.setPassword(decrypt);//密码
                break;
            case DataSourceType.DB2:
                datasourceInner.setJdbcUrl("jdbc:db2://" + tbDatasourceConfig.getConnectionIp()
                        + ":" + tbDatasourceConfig.getConnectionPort() + "/"
                        + tbDatasourceConfig.getSchemaDesc() + ":currentSchema=" + tbDatasourceConfig.getDb2Schema()+";");//url
//                datasourceInner.setDriverClassName("com.ibm.db2.jcc.DB2Driver");//db2要指定驱动名不然就默认COM.ibm.db2.jdbc.app.DB2Driver
                try {
                    datasourceInner.setDriverClass("com.ibm.db2.jcc.DB2Driver");
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
                //DB2的验证语句
//                datasourceInner.setValidationQuery("select 1 from sysibm.sysdummy1;");
                datasourceInner.setPassword(decrypt);//密码
                break;
            case DataSourceType.PostgreSQL:
                datasourceInner.setJdbcUrl("jdbc:postgresql://" + tbDatasourceConfig.getConnectionIp()
                        + ":" + tbDatasourceConfig.getConnectionPort() + "/"
                        + tbDatasourceConfig.getSchemaDesc());//url
                //PostgreSQL的验证语句
//                datasourceInner.setValidationQuery("select version();");
                datasourceInner.setPassword(decrypt);//密码
                break;
            case DataSourceType.EXCEL:
                datasourceInner.setJdbcUrl("jdbc:postgresql://" + tbDatasourceConfig.getConnectionIp()
                        + ":" + tbDatasourceConfig.getConnectionPort() + "/"
                        + tbDatasourceConfig.getSchemaDesc());//url
                //PostgreSQL的验证语句
//                datasourceInner.setValidationQuery("select version();");
                datasourceInner.setPassword(tbDatasourceConfig.getDatasourcePasswd());//密码
                break;
            default:
                datasourceInner.setJdbcUrl(null);
                break;
        }
    }

    /**
     * 声明连接池
     */
    private ComboPooledDataSource datasourceInner  = new ComboPooledDataSource();
    /**
     * 声明连接线程共享变量
     */
    private static ThreadLocal<Connection> container = new ThreadLocal<Connection>();
    /**
     * 声明连接池线程共享变量
     */
    private static ThreadLocal<ComboPooledDataSource> dsContainer = new ThreadLocal<ComboPooledDataSource>();

    /**
     * 获取数据连接
     * @return Connection
     */
    public Connection getConnection(){
        Connection conn =null;
        try{
            long starttime= System.currentTimeMillis();
            conn = datasourceInner.getConnection();
            long endtime= System.currentTimeMillis();
            System.out.println("usetime:"+(endtime-starttime)+"ms");
            System.out.println(Thread.currentThread().getName()+"连接已经开启......");
            container.set(conn);
            dsContainer.set(datasourceInner);
        }catch(Exception e){
            System.out.println("连接获取失败");
            e.printStackTrace();
        }
        return conn;
    }
    /***获取当前线程上的连接开启事务*/
    public void startTransaction(){
        Connection conn=container.get();//首先获取当前线程的连接
        if(conn==null){//如果连接为空
            conn=getConnection();//从连接池中获取连接
            container.set(conn);//将此连接放在当前线程上
            System.out.println(Thread.currentThread().getName()+"空连接从dataSource获取连接");
        }else{
            System.out.println(Thread.currentThread().getName()+"从缓存中获取连接");
        }
        try{
            conn.setAutoCommit(false);//开启事务
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /***提交事务*/
    public void commit(){
        try{
            Connection conn=container.get();//从当前线程上获取连接if(conn!=null){//如果连接为空,则不做处理
            if(null!=conn){
                conn.commit();//提交事务
                System.out.println(Thread.currentThread().getName()+"事务已经提交......");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /***回滚事务*/
    public  void rollback(){
        try{
            Connection conn=container.get();//检查当前线程是否存在连接
            if(conn!=null){
                conn.rollback();//回滚事务
                container.remove();//如果回滚了,就移除这个连接
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /***关闭连接*/
    public void close() {
        try {
            ComboPooledDataSource druidDataSource = dsContainer.get();
            Connection conn = container.get();
            if (conn != null && datasourceInner!=null) {
                conn.close();
                druidDataSource.close();
                System.out.println(Thread.currentThread().getName() + "连接关闭");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                dsContainer.remove();
                container.remove();//从当前线程移除连接切记
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

}
