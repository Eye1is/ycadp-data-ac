/*
 * JDBCUtils.java
 * Created at 2019/6/25
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class JDBCUtils {
    JDBCUtils(TBDatasourceConfig tbDatasourceConfig){
        dataSource  = new DruidDataSource();
        //    private final String driverClass = "com.mysql.cj.jdbc.Driver"; 默认驱动url可辨别
        dataSource.setUrl("jdbc:mysql://" + tbDatasourceConfig.getConnectionIp() + ":" + tbDatasourceConfig.getConnectionPort() + "/"
                + tbDatasourceConfig.getSchemaDesc()
                + "?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC");//url
        dataSource.setUsername(tbDatasourceConfig.getDatasourceUserName());//用户名
        dataSource.setPassword(tbDatasourceConfig.getDatasourcePasswd());//密码
        //配置初始化大小、最小、最大
        dataSource.setInitialSize(2);
        dataSource.setMaxActive(20);
        dataSource.setMinIdle(0);
        //配置从连接池获取连接等待超时的时间
        dataSource.setMaxWait(10000);
        dataSource.setValidationQuery("SELECT 1");
        //设置从连接池获取连接时是否检查连接有效性,true时,每次都检查;false时,不检查
        dataSource.setTestOnBorrow(false);
        //设置从连接池获取连接时是否检查连接有效性,
        //true时,如果连接空闲时间超过minEvictableIdleTimeMillis进行检查,否则不检查;false时,不检查
        dataSource.setTestWhileIdle(true);
        //设置往连接池归还连接时是否检查连接有效性,true时,每次都检查;false时,不检查
        dataSource.setTestOnReturn(false);
        dataSource.setPoolPreparedStatements(false);
        //配置间隔多久启动一次DestroyThread,对连接池内的连接才进行一次检测,单位是毫秒
        //检测时:1.如果连接空闲并且超过minIdle以外的连接,
        //如果空闲时间超过minEvictableIdleTimeMillis设置的值则直接物理关闭。2.在minIdle以内的不处理
        dataSource.setTimeBetweenEvictionRunsMillis(600000);
        //配置一个连接在池中最小生存的时间,单位是毫秒
        dataSource.setMinEvictableIdleTimeMillis(300000);
        //验证连接有效与否的SQL,不同的数据配置不同
        //检验连接是否有效的查询语句。如果数据库Driver支持ping()方法,
        //则优先使用ping()方法进行检查,否则使用validationQuery查询进行检查。(Oracle jdbc Driver目前不支持ping方法)
        dataSource.setValidationQuery("select 1");

        //连接泄露检查,打开removeAbandoned功能,
        //连接从连接池借出后,长时间不归还,将触发强制回连接.
        //回收周期随timeBetweenEvictionRunsMillis进行,如果连接为从连接池借出状态,并且未执行任何sql,
        //并且从借出时间起已超过removeAbandonedTimeout时间,则强制归还连接到连接池中
        dataSource.setRemoveAbandoned(true);
        //超时时间,秒
        dataSource.setRemoveAbandonedTimeout(80);
        //关闭abandoned连接时输出错误日志,这样出现连接泄露时可以通过错误日志定位忘记关闭连接的位置
        dataSource.setLogAbandoned(true);
        //打开PSCache,并且指定每个连接上PSCache的大小
        //只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true,参照druid的源码
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
    }

    private static DruidDataSource dataSource  = new DruidDataSource();
    //声明线程共享变量
    public static ThreadLocal<Connection> container = new ThreadLocal<Connection>();
    //配置说明,参考官方网址

    /**
     * 获取数据连接
     * @return
     */
    public  Connection getConnection(){
        Connection conn =null;
        try{
            conn = dataSource.getConnection();
            System.out.println(Thread.currentThread().getName()+"连接已经开启......");
            container.set(conn);
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
    //提交事务
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
            Connection conn = container.get();
            if (conn != null) {
                conn.close();
                System.out.println(Thread.currentThread().getName() + "连接关闭");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            try {
                container.remove();//从当前线程移除连接切记
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
