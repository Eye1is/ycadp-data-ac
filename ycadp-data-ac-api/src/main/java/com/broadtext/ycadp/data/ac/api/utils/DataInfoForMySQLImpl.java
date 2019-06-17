package com.broadtext.ycadp.data.ac.api.utils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.broadtext.ycadp.data.ac.api.vo.TBDatasourceConfigVo;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class DataInfoForMySQLImpl extends DaoFactory implements DataInfoInterface{
    private final static String driverClass = "com.mysql.cj.jdbc.Driver";
    private TBDatasourceConfigVo dsConfigVo;
    private PreparedStatement ps;
    private ResultSet rs;

    DataInfoForMySQLImpl(TBDatasourceConfigVo dsConfigVo){
        this.dsConfigVo = dsConfigVo;
    }

    @Override
    public List<String> getAllTables(TBDatasourceConfigVo dsConfigVo) {
        String url = "jdbc:mysql://" + this.dsConfigVo.getConnectionIp() + ":" + this.dsConfigVo.getConnectionPort() + "/"
                + this.dsConfigVo.getDatasourceName()
                + "?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC";
        DruidDynamicDataSource dataSource = DruidDynamicDataSource.getInstance();
        try {
            DruidPooledConnection dsConnection =
                    dataSource.getDataSourceConnection(driverClass, url,
                            this.dsConfigVo.getDatasourceUserName(), this.dsConfigVo.getDatasourcePasswd());
            this.ps = dsConnection.prepareStatement("SHOW TABLES;");
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
        }
        return null;
    }

    @Override
    public List getAllData(TBDatasourceConfigVo dsConfigVo,String sql) {
        System.out.println(" === " + sql);
        JdbcUtils.closeResultSet(this.rs);
        JdbcUtils.closeStatement(this.ps);
        String url = "jdbc:mysql://" + this.dsConfigVo.getConnectionIp() + ":" + this.dsConfigVo.getConnectionPort() + "/"
                + this.dsConfigVo.getDatasourceName()
                + "?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC";
        DruidDynamicDataSource dataSource = DruidDynamicDataSource.getInstance();
        try {
            DruidPooledConnection dsConnection =
                    dataSource.getDataSourceConnection(driverClass, url,
                            this.dsConfigVo.getDatasourceUserName(), this.dsConfigVo.getDatasourcePasswd());
            this.ps = dsConnection.prepareStatement(sql);
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
        }
        return null;
    }

    @Override
    public Integer getDataCount(TBDatasourceConfigVo dsConfigVo, String sql) {
        String url = "jdbc:mysql://" + this.dsConfigVo.getConnectionIp() + ":" + this.dsConfigVo.getConnectionPort() + "/"
                + this.dsConfigVo.getDatasourceName()
                + "?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC";
        DruidDynamicDataSource dataSource = DruidDynamicDataSource.getInstance();
        try {
            DruidPooledConnection dsConnection =
                    dataSource.getDataSourceConnection(driverClass, url,
                            this.dsConfigVo.getDatasourceUserName(), this.dsConfigVo.getDatasourcePasswd());
            this.ps = dsConnection.prepareStatement("SELECT COUNT(1) RECORD FROM (" + sql + ") DATACOUNT");
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
        }
        return null;
    }

    @Override
    public void crateTable(TBDatasourceConfigVo dsConfigVo, String sql) {

    }

    @Override
    public void update(TBDatasourceConfigVo dsConfigVo, String sql) {

    }

    @Override
    public void insert(TBDatasourceConfigVo dsConfigVo, String sql) {

    }

    @Override
    public String query(TBDatasourceConfigVo dsConfigVo, String sql) {
        return null;
    }

    @Override
    public String delete(TBDatasourceConfigVo dsConfigVo, String sql) {
        return null;
    }
}
