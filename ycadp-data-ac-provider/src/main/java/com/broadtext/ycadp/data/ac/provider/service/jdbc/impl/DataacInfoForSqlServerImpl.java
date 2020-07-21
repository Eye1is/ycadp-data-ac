package com.broadtext.ycadp.data.ac.provider.service.jdbc.impl;

import com.broadtext.ycadp.core.common.service.BaseServiceImpl;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldInfoVo;
import com.broadtext.ycadp.data.ac.provider.repository.DataacRepository;
import com.broadtext.ycadp.data.ac.provider.service.jdbc.DataacInfoService;
import com.broadtext.ycadp.data.ac.provider.utils.AesUtil;
import com.broadtext.ycadp.data.ac.provider.utils.DruidUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * sqlServer数据源连接的具体实现
 *
 * @author ouhaoliang
 */
@Service("sqlServer")
@Transactional
public class DataacInfoForSqlServerImpl extends BaseServiceImpl<TBDatasourceConfig, String, DataacRepository> implements DataacInfoService {

    @Autowired
    private DataacRepository dataacRepository;

    @Override
    public List<String> getAllTables(TBDatasourceConfig tbDatasourceConfig) {
        //获取当前正在执行的线程的名字
        System.out.println(Thread.currentThread().getName());
        DruidUtil jdbcUtils = new DruidUtil(tbDatasourceConfig);
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = jdbcUtils.getConnection();
            ps = connection.prepareStatement("select * from sysobjects where xtype='U';");
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
    public List<Map<String, Object>> getAllData(TBDatasourceConfig tbDatasourceConfig, String sql) {
        System.out.println(" === " + sql);
        DruidUtil jdbcUtils = new DruidUtil(tbDatasourceConfig);
        Connection connection = null;
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
            DruidUtil jdbcUtils = new DruidUtil(tbDatasourceConfig);
            Connection connection = null;
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
        DruidUtil jdbcUtils = new DruidUtil(tbDatasourceConfig);
        Connection connection = null;
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
            DruidUtil jdbcUtils = new DruidUtil(tbDatasourceConfig);
            Connection connection = null;
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

            DruidUtil jdbcUtils = new DruidUtil(tbDatasourceConfig);
            Connection connection = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            PreparedStatement ps1 = null;
            ResultSet rs1 = null;
            try {
                connection = jdbcUtils.getConnection();
                ps = connection.prepareStatement("select table_name,column_name,data_type from information_schema.columns where table_name = '" + table + "';");
                rs = ps.executeQuery();
                List<FieldInfoVo> list = new ArrayList<FieldInfoVo>();
                FieldInfoVo field;
                while (rs.next()) {
                    field = new FieldInfoVo();
                    field.setFieldName(rs.getString(2));
                    field.setFieldType(rs.getString(3));
                    list.add(field);
                }


                ps1 = connection.prepareStatement("select a.name  table_name,b.name  column_name,C.value  column_description\n" +
                        "from sys.tables a\n" +
                        "inner join sys.columns b on b.object_id = a.object_id\n" +
                        "left join sys.extended_properties c on c.major_id = b.object_id and c.minor_id = b.column_id\n" +
                        "where a.name = '" + table + "';");
                rs1 = ps1.executeQuery();
                List<FieldInfoVo> list1 = new ArrayList<FieldInfoVo>();
                while (rs1.next()) {
                    field = new FieldInfoVo();
                    field.setFieldName(rs1.getString(2));
                    field.setFieldDesign(rs1.getString(3));
                    list1.add(field);
                }
                return compareListEqData(list1, list);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                JdbcUtils.closeResultSet(rs1);
                JdbcUtils.closeStatement(ps1);
                jdbcUtils.close();
            }
        }
        return null;
    }


    public static List<FieldInfoVo> compareListEqData(List<FieldInfoVo> oneList, List<FieldInfoVo> twoList) {
        return oneList.stream().map(fieldInfoVo -> twoList.stream()
                .filter(log -> fieldInfoVo.getFieldName().equals(log.getFieldName()))
                .findAny()
                .map(log -> {
                    log.setFieldDesign(fieldInfoVo.getFieldDesign());
                    return log;
                }).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void crateTable(String datasourceId, String sql) throws Exception {

    }

    @Override
    public void update(String datasourceId, String sql) throws Exception {

    }

    @Override
    public void insert(String datasourceId, String sql) throws Exception {

    }

    @Override
    public String query(String datasourceId, String sql) throws Exception {
        return null;
    }

    @Override
    public void delete(String datasourceId, String sql) throws Exception {

    }

    @Override
    public void truncate(String datasourceId, String sql) throws Exception {

    }

    @Override
    public Map<Boolean, String> check(TBDatasourceConfig tbDatasourceConfig) {
        //获取当前正在执行的线程的名字
        System.out.println(Thread.currentThread().getName());
        Map<Boolean, String> checkMap = new HashMap<>();
        String url = "jdbc:sqlserver://" + tbDatasourceConfig.getConnectionIp() + ":" + tbDatasourceConfig.getConnectionPort() + ";DatabaseName=" + tbDatasourceConfig.getSchemaDesc() + ";";
        try {
            String driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            checkMap.put(false, "连接失败,缺少驱动");
            return checkMap;
        }
        Connection connection = null;
        try {
            String decrypt = AesUtil.decrypt(tbDatasourceConfig.getDatasourcePasswd(), "aaaaaaaaaaaaaaaaaaaaaaaa");
            connection = DriverManager.getConnection(url, tbDatasourceConfig.getDatasourceUserName(),
                    decrypt);
            checkMap.put(true, "连接成功");
            return checkMap;
        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
//            if (errorCode == MysqlCheckErrorCode.ERROR_CONNECTION) {
//                checkMap.put(false, "网络异常,IP地址或者端口有误:" + e.getMessage());
//            } else if (errorCode == MysqlCheckErrorCode.ERROR_DATASOURCE) {
//                checkMap.put(false, "连接失败,错误的数据库名:" + e.getMessage());
//            } else if (errorCode == MysqlCheckErrorCode.ERROR_USERORPW) {
//                checkMap.put(false, "连接失败,用户名或密码错误:" + e.getMessage());
//            } else if (errorCode == MysqlCheckErrorCode.ERROR_ACCESS) {
//                checkMap.put(false, "连接失败,无权访问:" + e.getMessage());
//            } else {
            checkMap.put(false, "连接失败,系统错误:" + e.getMessage());
//            }
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
            DruidUtil jdbcUtils = new DruidUtil(byId);
            Connection connection = null;
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
    public String getLimitString(String sql, int skipResults, int maxResults) {
        return "select top " + maxResults + " * from (select row_number() over(order by id asc) as rownumber,* from " + sql + ") temp_row where rownumber>" + skipResults;
    }
}
