/*
 * DataacInfoForPostgresqlImpl.java
 * Created at 2020/1/6
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.service.jdbc.impl;

import com.broadtext.ycadp.core.common.service.BaseServiceImpl;
import com.broadtext.ycadp.data.ac.api.constants.DataSourceType;
import com.broadtext.ycadp.data.ac.api.constants.PostgresqlCheckErrorCode;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldInfoVo;
import com.broadtext.ycadp.data.ac.provider.repository.DataacRepository;
import com.broadtext.ycadp.data.ac.provider.service.jdbc.DataacInfoService;
import com.broadtext.ycadp.data.ac.provider.utils.JDBCUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PostgreSQL数据源的具体实现
 */
@Service("postgresql")
@Transactional
public class DataacInfoForPostgreSQLImpl extends BaseServiceImpl<TBDatasourceConfig, String, DataacRepository> implements DataacInfoService {

    @Autowired
    private DataacRepository dataacRepository;
    @Value("${dataac.datasourceIp}")
    private String datasourceIp;
    @Value("${dataac.datasourcePort}")
    private Integer datasourcePort;
    @Value("${dataac.schemaDesc}")
    private String schemaDesc;
    @Value("${dataac.datasourceUserName}")
    private String datasourceUserName;
    @Value("${dataac.datasourcePasswd}")
    private String datasourcePasswd;


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
            ps = connection.prepareStatement("select tablename from pg_tables");
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
    public List getAllData(TBDatasourceConfig tbDatasourceConfig, String sql) {
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
    public List<Map<String, Object>> getAllDataWithDict(String datasourceId, String sql, Map<String, List<FieldDictVo>> dictMap) {
        System.out.println(" === " + sql);
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);
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
                    rowData = new LinkedHashMap<String, Object>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        if (rs.getObject(i) == null) {
                            rowData.put(md.getColumnLabel(i), "");
                        } else {
                            if (null != dictMap && 0 < dictMap.size()) {
                                if (dictMap.containsKey(md.getColumnLabel(i))) {
                                    boolean isDict = false;
                                    List<FieldDictVo> dicts = dictMap.get(md.getColumnLabel(i));
                                    for (FieldDictVo dict : dicts) {
                                        if (dict.getDictValue().equals(rs.getString(i))) {
                                            rowData.put(md.getColumnLabel(i), dict.getDictText());
                                            isDict = true;
                                        }
                                    }
                                    if (!isDict) {
                                        rowData.put(md.getColumnLabel(i), rs.getObject(i));
                                    }
                                } else {
                                    rowData.put(md.getColumnLabel(i), rs.getObject(i));
                                }
                            } else {
                                rowData.put(md.getColumnLabel(i), rs.getObject(i));
                            }
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
        } else {
            TBDatasourceConfig tbDatasourceConfig = new TBDatasourceConfig();
            tbDatasourceConfig.setConnectionIp(datasourceIp);
            tbDatasourceConfig.setConnectionPort(datasourcePort);
            tbDatasourceConfig.setSchemaDesc(schemaDesc);
            tbDatasourceConfig.setDatasourceUserName(datasourceUserName);
            tbDatasourceConfig.setDatasourcePasswd(datasourcePasswd);
            tbDatasourceConfig.setDatasourceType(DataSourceType.PostgreSQL);
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
                    rowData = new LinkedHashMap<String, Object>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        if (rs.getObject(i) == null) {
                            rowData.put(md.getColumnLabel(i), "");
                        } else {
                            if (null != dictMap && 0 < dictMap.size()) {
                                if (dictMap.containsKey(md.getColumnLabel(i))) {
                                    boolean isDict = false;
                                    List<FieldDictVo> dicts = dictMap.get(md.getColumnLabel(i));
                                    for (FieldDictVo dict : dicts) {
                                        if (dict.getDictValue().equals(rs.getString(i))) {
                                            rowData.put(md.getColumnLabel(i), dict.getDictText());
                                            isDict = true;
                                        }
                                    }
                                    if (!isDict) {
                                        rowData.put(md.getColumnLabel(i), rs.getObject(i));
                                    }
                                } else {
                                    rowData.put(md.getColumnLabel(i), rs.getObject(i));
                                }
                            } else {
                                rowData.put(md.getColumnLabel(i), rs.getObject(i));
                            }
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
        }
        return null;
    }

    @Override
    public List<FieldDictVo> getDictData(String datasourceId, String dictSql, String key) {
        List<FieldDictVo> list = new ArrayList<>();
        List<Map<String, Object>> data = getAllDataWithDict(datasourceId, dictSql.replace("?", key), null);
        for (Map<String, Object> map : data) {
            list.add(new FieldDictVo(map.get("_value").toString(),
                    map.get("_text").toString()));
        }
        return list;
    }

    @Override
    public Integer getDataCount(TBDatasourceConfig tbDatasourceConfig, String sql) {
        JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
        Connection connection;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = jdbcUtils.getConnection();
            ps = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = ps.executeQuery();
            if (rs == null) {
                return 0;
            }
            rs.last();
            return rs.getRow();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            jdbcUtils.close();
        }
        return 0;
    }

    @Override
    public Integer getDataCount(String datasourceId, String sql) {
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);
            JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
            Connection connection;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                connection = jdbcUtils.getConnection();
                ps = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                rs = ps.executeQuery();
                if (rs == null) {
                    return 0;
                }
                rs.last();
                return rs.getRow();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                jdbcUtils.close();
            }
        } else {
            TBDatasourceConfig tbDatasourceConfig = new TBDatasourceConfig();
            tbDatasourceConfig.setConnectionIp(datasourceIp);
            tbDatasourceConfig.setConnectionPort(datasourcePort);
            tbDatasourceConfig.setSchemaDesc(schemaDesc);
            tbDatasourceConfig.setDatasourceUserName(datasourceUserName);
            tbDatasourceConfig.setDatasourcePasswd(datasourcePasswd);
            tbDatasourceConfig.setDatasourceType(DataSourceType.PostgreSQL);
            JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
            Connection connection;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                connection = jdbcUtils.getConnection();
                ps = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                rs = ps.executeQuery();
                if (rs == null) {
                    return 0;
                }
                rs.last();
                return rs.getRow();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                jdbcUtils.close();
            }
        }
        return 0;
    }

    @Override
    public List<FieldInfoVo> getAllFields(String datasourceId, String table) {
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);

            JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
            Connection connection;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                connection = jdbcUtils.getConnection();
                ps = connection.prepareStatement("SELECT a.attname as \"字段名\",col_description(a.attrelid,a.attnum) as \"注释\",concat_ws('',t.typname,SUBSTRING(format_type(a.atttypid,a.atttypmod) from '\\(.*\\)')) as \"字段类型\" FROM pg_class as c,pg_attribute as a, pg_type as t WHERE c.relname = '"+table+"' and a.atttypid = t.oid and a.attrelid = c.oid and a.attnum>0");
                rs = ps.executeQuery();
                List<FieldInfoVo> list = new ArrayList<FieldInfoVo>();
                FieldInfoVo field;
                while (rs.next()) {
                    field = new FieldInfoVo();
                    field.setFieldName(rs.getString(1));
                    field.setFieldType(rs.getString(3));
                    field.setFieldDesign(rs.getString(2));
                    list.add(field);
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                jdbcUtils.close();
            }
        }
        return null;
    }

    @Override
    public void crateTable(String datasourceId, String sql) {
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);
            JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
            Connection connection;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                connection = jdbcUtils.getConnection();
                connection.setAutoCommit(false);
                ps = connection.prepareStatement(sql);
                ps.execute();
                connection.commit();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                jdbcUtils.close();
            }
        }
    }

    @Override
    public void update(String datasourceId, String sql) {
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);
            JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
            Connection connection;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                connection = jdbcUtils.getConnection();
                connection.setAutoCommit(false);
                ps = connection.prepareStatement(sql);
                ps.execute();
                connection.commit();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                jdbcUtils.close();
            }
        }
    }

    @Override
    public void insert(String datasourceId, String sql) {
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);
            JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
            Connection connection;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                connection = jdbcUtils.getConnection();
                connection.setAutoCommit(false);
                ps = connection.prepareStatement(sql);
                ps.execute();
                connection.commit();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                jdbcUtils.close();
            }
        }
    }

    @Override
    public String query(String datasourceId, String sql) {
        String s = "";
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);
            JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
            Connection connection;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                connection = jdbcUtils.getConnection();
                connection.setAutoCommit(false);
                ps = connection.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    s = rs.getString(1);
                }
                connection.commit();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                jdbcUtils.close();
            }
        }
        return s;
    }

    @Override
    public void delete(String datasourceId, String sql) {
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);
            JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
            Connection connection;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                connection = jdbcUtils.getConnection();
                ps = connection.prepareStatement(sql);
                ps.execute();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                jdbcUtils.close();
            }
        }
    }

    @Override
    public void truncate(String datasourceId, String sql) throws Exception {
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);
            JDBCUtils jdbcUtils = new JDBCUtils(tbDatasourceConfig);
            Connection connection;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                connection = jdbcUtils.getConnection();
                connection.setAutoCommit(false);
                ps = connection.prepareStatement(sql);
                ps.execute();
                connection.commit();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                jdbcUtils.close();
            }
        }
    }

    @Override
    public Map<Boolean, String> check(TBDatasourceConfig tbDatasourceConfig) {
        //获取当前正在执行的线程的名字
        System.out.println(Thread.currentThread().getName());
        Map<Boolean, String> checkMap = new HashMap<>();
        String url = "jdbc:postgresql://" + tbDatasourceConfig.getConnectionIp()
                + ":" + tbDatasourceConfig.getConnectionPort() + "/"
                + tbDatasourceConfig.getSchemaDesc();
        try {
            String driverClass = "org.postgresql.Driver";
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            checkMap.put(false, "连接失败,缺少驱动");
            return checkMap;
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, tbDatasourceConfig.getDatasourceUserName(),
                    tbDatasourceConfig.getDatasourcePasswd());
            checkMap.put(true, "连接成功");
            return checkMap;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getErrorCode());
            int errorCode = e.getErrorCode();
            if (errorCode == PostgresqlCheckErrorCode.ERROR_CONNECTION) {
                checkMap.put(false, "网络异常,IP地址或者端口有误:"+e.getMessage());
            } else if (errorCode == PostgresqlCheckErrorCode.ERROR_DATASOURCE) {
                checkMap.put(false, "连接失败,错误的数据库名:"+e.getMessage());
            } else if (errorCode == PostgresqlCheckErrorCode.ERROR_USERORPW) {
                checkMap.put(false, "连接失败,用户名或密码错误:"+e.getMessage());
            } else if (errorCode == PostgresqlCheckErrorCode.ERROR_ACCESS) {
                checkMap.put(false, "连接失败,无权访问:"+e.getMessage());
            } else {
                checkMap.put(false, "连接失败,系统错误:"+e.getMessage());
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

    @Override
    public List<String> getDistinctFields(String datasourceId, String sql) {
        TBDatasourceConfig byId = this.findById(datasourceId);
        List<String> distinctFields = new ArrayList<>();
        if (byId != null) {
            JDBCUtils jdbcUtils = new JDBCUtils(byId);
            Connection connection;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                connection = jdbcUtils.getConnection();
                ps = connection.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String aa = rs.getString(1);
                    distinctFields.add(rs.getString(1));
                }
                return distinctFields;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                jdbcUtils.close();
            }
        }
        return null;
    }

    @Override
    public String getLimitString(String sql, int skipResults, int maxResults) throws Exception {
        return sql + " limit " + maxResults + " offset " + skipResults + ";";
    }
}
