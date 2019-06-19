package com.broadtext.ycadp.data.ac.api.utils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class DataInfoForMySQLImpl extends DaoFactory {
    private final static String driverClass = "com.mysql.cj.jdbc.Driver";
    private TBDatasourceConfig tbDatasourceConfig;
    private PreparedStatement ps;
    private ResultSet rs;
    private DruidPooledConnection dsConnection;
    DataInfoForMySQLImpl(TBDatasourceConfig tbDatasourceConfig){
        this.tbDatasourceConfig = tbDatasourceConfig;
    }

    @Override
    public List<String> getAllTables(TBDatasourceConfig tbDatasourceConfig) {
        String url = "jdbc:mysql://" + this.tbDatasourceConfig.getConnectionIp() + ":" + this.tbDatasourceConfig.getConnectionPort() + "/"
                + this.tbDatasourceConfig.getSchemaDesc()
                + "?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC";
        DruidDynamicDataSource dataSource = DruidDynamicDataSource.getInstance();
        try {
            this.dsConnection =
                    dataSource.getDataSourceConnection(driverClass, url,
                            this.tbDatasourceConfig.getDatasourceUserName(), this.tbDatasourceConfig.getDatasourcePasswd());
            this.ps = this.dsConnection.prepareStatement("SHOW TABLES;");
            this.rs = this.ps.executeQuery();
            List<String> _list = new ArrayList<String>();
            while (this.rs.next()) {
                _list.add(this.rs.getString(1));
            }
            return _list;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(this.rs);
            JdbcUtils.closeStatement(this.ps);
            try {
                this.dsConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> getAllData(TBDatasourceConfig tbDatasourceConfig,String sql) {
        System.out.println(" === " + sql);
        JdbcUtils.closeResultSet(this.rs);
        JdbcUtils.closeStatement(this.ps);
        String url = "jdbc:mysql://" + this.tbDatasourceConfig.getConnectionIp() + ":" + this.tbDatasourceConfig.getConnectionPort() + "/"
                + this.tbDatasourceConfig.getSchemaDesc()
                + "?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC";
        DruidDynamicDataSource dataSource = DruidDynamicDataSource.getInstance();
        try {
            this.dsConnection =
                    dataSource.getDataSourceConnection(driverClass, url,
                            this.tbDatasourceConfig.getDatasourceUserName(), this.tbDatasourceConfig.getDatasourcePasswd());
            this.ps = this.dsConnection.prepareStatement(sql);
            this.rs = this.ps.executeQuery();
            if (this.rs == null) return Collections.EMPTY_LIST;
            ResultSetMetaData md = this.rs.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等
            int columnCount = md.getColumnCount(); //返回此 ResultSet 对象中的列数
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Map<String, Object> rowData;
            while (this.rs.next()) {
                rowData = new LinkedHashMap<String, Object>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    if (this.rs.getObject(i) == null) {
                        rowData.put(md.getColumnLabel(i), "");
                    } else {
                        rowData.put(md.getColumnLabel(i), this.rs.getObject(i));
                    }

                }
                list.add(rowData);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(this.rs);
            JdbcUtils.closeStatement(this.ps);
            try {
                this.dsConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public Integer getDataCount(TBDatasourceConfig tbDatasourceConfig, String sql) {
        String url = "jdbc:mysql://" + this.tbDatasourceConfig.getConnectionIp() + ":" + this.tbDatasourceConfig.getConnectionPort() + "/"
                + this.tbDatasourceConfig.getSchemaDesc()
                + "?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC";
        DruidDynamicDataSource dataSource = DruidDynamicDataSource.getInstance();
        try {
            this.dsConnection =
                    dataSource.getDataSourceConnection(driverClass, url,
                            this.tbDatasourceConfig.getDatasourceUserName(), this.tbDatasourceConfig.getDatasourcePasswd());
            this.ps = this.dsConnection.prepareStatement("SELECT COUNT(1) RECORD FROM (" + sql + ") DATACOUNT");
            this.rs = this.ps.executeQuery();
            if (this.rs == null) {
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
            JdbcUtils.closeResultSet(this.rs);
            JdbcUtils.closeStatement(this.ps);
            try {
                this.dsConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        Map<Boolean, String> checkMap = new HashMap<>();
        String url = "jdbc:mysql://" + this.tbDatasourceConfig.getConnectionIp() + ":" 
                + this.tbDatasourceConfig.getConnectionPort() + "/"
                + this.tbDatasourceConfig.getSchemaDesc()
                + "?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC";
        DruidDynamicDataSource dataSource = DruidDynamicDataSource.getInstance();
        try {
            this.dsConnection = dataSource.getDataSourceConnection(driverClass, url,
                    this.tbDatasourceConfig.getDatasourceUserName(),
                    this.tbDatasourceConfig.getDatasourcePasswd());
            checkMap.put(true,"连接成功");
            return checkMap;
        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            if (errorCode == 0) {
                checkMap.put(false, "网络异常,IP地址或者端口有误");
            } else if (errorCode == 1049) {
                checkMap.put(false, "连接失败,错误的数据库名");
            } else if (errorCode == 1045) {
                checkMap.put(false, "连接失败,用户名或密码错误");
            } else if (errorCode == 1142) {
                checkMap.put(false, "连接失败,无权访问");
            } else {
                checkMap.put(false, "连接失败,系统错误");
            }
            return checkMap;
        } finally {
            JdbcUtils.closeResultSet(this.rs);
            JdbcUtils.closeStatement(this.ps);
            try {
                this.dsConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
