package com.broadtext.ycadp.data.ac.api.utils;

import com.broadtext.ycadp.data.ac.api.constants.CheckErrorCode;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * MYSQL数据源连接的具体实现
 * @author ouhaoliang
 */
public class DataInfoForMySQLImpl extends DaoFactory {
    /**
     *
     */
    private TBDatasourceConfig tbDatasourceConfig;
    DataInfoForMySQLImpl(TBDatasourceConfig tbDatasourceConfig){
        this.tbDatasourceConfig = tbDatasourceConfig;
    }

    @Override
    public List<String> getAllTables(TBDatasourceConfig tbDatasourceConfig) {
        //获取当前正在执行的线程的名字
        System.out.println(Thread.currentThread().getName());
        JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
        Connection connection;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = jdbcUtils.getConnection();
            ps = connection.prepareStatement("SHOW TABLES;");
            rs = ps.executeQuery();
            List<String> list = new ArrayList<String>();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            jdbcUtils.close();
        }
        return null;
    }

    @Override
    public List<Map<String,Object>> getAllData(TBDatasourceConfig tbDatasourceConfig, String sql) {
        System.out.println(" === " + sql);
        JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
        Connection connection;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = jdbcUtils.getConnection();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs == null) {
                return Collections.EMPTY_LIST;
            }
            ResultSetMetaData md = rs.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等
            int columnCount = md.getColumnCount(); //返回此 ResultSet 对象中的列数
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> rowData;
            while (rs.next()) {
                rowData = new LinkedHashMap<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    if (rs.getObject(i) == null) {
                        rowData.put(md.getColumnLabel(i), "");
                    } else {
                        rowData.put(md.getColumnLabel(i), rs.getObject(i));
                    }

                }
                list.add(rowData);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            jdbcUtils.close();
        }
        return null;
    }

    @Override
    public Integer getDataCount(TBDatasourceConfig tbDatasourceConfig, String sql) {
        JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
        Connection connection;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = jdbcUtils.getConnection();
            ps = connection.prepareStatement("SELECT COUNT(1) RECORD FROM (" + sql + ") DATACOUNT");
            rs = ps.executeQuery();
            if (rs == null) {
                return 0;
            }
            int rowCount = 0;
            if (rs.next()) {
                rowCount = rs.getInt("RECORD");
            }
            return rowCount;
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            jdbcUtils.close();
        }
        return null;
    }

    @Override
    public void crateTable(TBDatasourceConfig tbDatasourceConfig, String sql) {

    }

    @Override
    public void update(TBDatasourceConfig tbDatasourceConfig, String sql) {

    }

    @Override
    public void insert(TBDatasourceConfig tbDatasourceConfig, String sql) {

    }

    @Override
    public String query(TBDatasourceConfig tbDatasourceConfig, String sql) {
        return null;
    }

    @Override
    public String delete(TBDatasourceConfig tbDatasourceConfig, String sql) {
        return null;
    }

    @Override
    public Map<Boolean, String> check(TBDatasourceConfig tbDatasourceConfig) {
        //获取当前正在执行的线程的名字
        System.out.println(Thread.currentThread().getName());
        Map<Boolean, String> checkMap = new HashMap<>();
        String url = "jdbc:mysql://" + this.tbDatasourceConfig.getConnectionIp() + ":"
                + this.tbDatasourceConfig.getConnectionPort() + "/"
                + this.tbDatasourceConfig.getSchemaDesc()
                + "?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC";
        try {
            String driverClass = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverClass);
        }
        catch(ClassNotFoundException e) {
            checkMap.put(false,"连接失败,缺少驱动");
            return checkMap;
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, this.tbDatasourceConfig.getDatasourceUserName(),
                    this.tbDatasourceConfig.getDatasourcePasswd());
            checkMap.put(true,"连接成功");
            return checkMap;
        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            if (errorCode == CheckErrorCode.ERROR_CONNECTION) {
                checkMap.put(false, "网络异常,IP地址或者端口有误");
            } else if (errorCode == CheckErrorCode.ERROR_DATASOURCE) {
                checkMap.put(false, "连接失败,错误的数据库名");
            } else if (errorCode == CheckErrorCode.ERROR_USERORPW) {
                checkMap.put(false, "连接失败,用户名或密码错误");
            } else if (errorCode == CheckErrorCode.ERROR_ACCESS) {
                checkMap.put(false, "连接失败,无权访问");
            } else {
                checkMap.put(false, "连接失败,系统错误");
            }
            return checkMap;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
