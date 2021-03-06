package com.broadtext.ycadp.data.ac.provider.service.jdbc.impl;

import com.broadtext.ycadp.core.common.service.BaseServiceImpl;
import com.broadtext.ycadp.data.ac.api.constants.DataSourceType;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.vo.FieldDictVo;
import com.broadtext.ycadp.data.ac.api.vo.FieldInfoVo;
import com.broadtext.ycadp.data.ac.api.vo.PostgreConfigVo;
import com.broadtext.ycadp.data.ac.provider.repository.DataacRepository;
import com.broadtext.ycadp.data.ac.provider.service.jdbc.DataacInfoService;
import com.broadtext.ycadp.data.ac.provider.utils.JDBCUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.*;
import java.util.*;

/**
 * Excel数据源连接的具体实现
 *
 * @author xuchenglong
 */
@Service("excel")
@Transactional
public class DataacInfoForExcelImpl extends BaseServiceImpl<TBDatasourceConfig, String, DataacRepository> implements DataacInfoService {
    @Autowired
    private DataacRepository dataacRepository;
    @Value("${dataac.datasourceIp}")
    private String datasourceIpConfig;
    @Value("${dataac.datasourcePort}")
    private String datasourcePort;
    @Value("${dataac.schemaDesc}")
    private String schemaDesc;
    @Value("${dataac.datasourceUserName}")
    private String datasourceUserName;
    @Value("${dataac.datasourcePasswd}")
    private String datasourcePasswd;


    @Override
    public List<String> getAllTables(TBDatasourceConfig tbDatasourceConfig) throws Exception {
        return null;
    }

    @Override
    public List getAllData(TBDatasourceConfig tbDatasourceConfig, String sql) throws Exception {
        System.out.println(" === " + sql);
        tbDatasourceConfig.setConnectionIp(datasourceIpConfig);
        tbDatasourceConfig.setConnectionPort(datasourcePort == null || "".equals(datasourcePort) ? 5432 : Integer.parseInt(datasourcePort));
        tbDatasourceConfig.setSchemaDesc(schemaDesc);
        tbDatasourceConfig.setDatasourceUserName(datasourceUserName);
        tbDatasourceConfig.setDatasourcePasswd(datasourcePasswd);
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
    public List<Map<String, Object>> getAllDataWithDict(String datasourceId, String sql, Map<String, List<FieldDictVo>> dictMap) throws Exception {
        System.out.println(" === " + sql);
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
            TBDatasourceConfig tbDatasourceConfig = new TBDatasourceConfig();
            tbDatasourceConfig.setDatasourceType(DataSourceType.EXCEL);
            tbDatasourceConfig.setConnectionIp(datasourceIpConfig);
            tbDatasourceConfig.setConnectionPort(datasourcePort == null || "".equals(datasourcePort) ? 5432 : Integer.parseInt(datasourcePort));
            tbDatasourceConfig.setSchemaDesc(schemaDesc);
            tbDatasourceConfig.setDatasourceUserName(datasourceUserName);
            tbDatasourceConfig.setDatasourcePasswd(datasourcePasswd);
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
                                    if(!isDict){
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
    public List<FieldDictVo> getDictData(String datasourceId, String dictSql, String key) throws Exception {
        List<FieldDictVo> list = new ArrayList<>();
        List<Map<String, Object>> data = getAllDataWithDict(datasourceId,dictSql.replace("?", key), null);
        for (Map<String, Object> map : data) {
            list.add(new FieldDictVo(map.get("_value").toString(),
                    map.get("_text").toString()));
        }
        return list;
    }

    @Override
    public Integer getDataCount(TBDatasourceConfig tbDatasourceConfig, String sql) throws Exception {
        tbDatasourceConfig.setConnectionIp(datasourceIpConfig);
        tbDatasourceConfig.setConnectionPort(datasourcePort == null || "".equals(datasourcePort) ? 5432 : Integer.parseInt(datasourcePort));
        tbDatasourceConfig.setSchemaDesc(schemaDesc);
        tbDatasourceConfig.setDatasourceUserName(datasourceUserName);
        tbDatasourceConfig.setDatasourcePasswd(datasourcePasswd);
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
    public Integer getDataCount(String datasourceId, String sql) throws Exception {
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
//            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);
            TBDatasourceConfig tbDatasourceConfig = new TBDatasourceConfig();
            tbDatasourceConfig.setDatasourceType(DataSourceType.EXCEL);
            tbDatasourceConfig.setConnectionIp(datasourceIpConfig);
            tbDatasourceConfig.setConnectionPort(datasourcePort == null || "".equals(datasourcePort) ? 5432 : Integer.parseInt(datasourcePort));
            tbDatasourceConfig.setSchemaDesc(schemaDesc);
            tbDatasourceConfig.setDatasourceUserName(datasourceUserName);
            tbDatasourceConfig.setDatasourcePasswd(datasourcePasswd);
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
    public List<FieldInfoVo> getAllFields(String datasourceId, String table) throws Exception {
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
//            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);
            TBDatasourceConfig tbDatasourceConfig = new TBDatasourceConfig();
            tbDatasourceConfig.setDatasourceType(DataSourceType.EXCEL);
            tbDatasourceConfig.setConnectionIp(datasourceIpConfig);
            tbDatasourceConfig.setConnectionPort(datasourcePort == null || "".equals(datasourcePort) ? 5432 : Integer.parseInt(datasourcePort));
            tbDatasourceConfig.setSchemaDesc(schemaDesc);
            tbDatasourceConfig.setDatasourceUserName(datasourceUserName);
            tbDatasourceConfig.setDatasourcePasswd(datasourcePasswd);
//            PostgreConfigVo pVo = new PostgreConfigVo();
//            pVo.setUrl("jdbc:postgresql://192.168.16.171:5432/postgres")
//                    .setUser("postgres")
//                    .setPwd("postgres");
//            Connection coon = null;
//            ResultSet rs = null;
//            Statement stmt = null;
//            try {
//                Class.forName("org.postgresql.Driver");
//                coon = DriverManager.getConnection(pVo.getUrl(), pVo.getUser(), pVo.getPwd());
//                coon.setAutoCommit(false);
//                System.out.println("开启postgre数据库成功");
//                stmt = coon.createStatement();
//                stmt.executeUpdate("");
//                stmt.close();
//                coon.commit();
//                coon.close();
//            } catch (ClassNotFoundException e) {
//                System.out.println("装在jdbc驱动失败");
//                e.printStackTrace();
//                return null;
//            } catch (SQLException e) {
//                System.out.println("无法连接数据库");
//                e.printStackTrace();
//                return null;
//            }
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
    public Map<Boolean, String> check(TBDatasourceConfig tbDatasourceConfig) throws Exception {
        return null;
    }

    @Override
    public List<String> getDistinctFields(String datasourceId, String sql) throws Exception {
        return null;
    }

    @Override
    public String getLimitString(String sql, int skipResults, int maxResults) throws Exception {
        return null;
    }
}
