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
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Override
    public List<String> getAllTables(TBDatasourceConfig tbDatasourceConfig) throws Exception {
        return null;
    }

    @Override
    public List getAllData(TBDatasourceConfig tbDatasourceConfig, String sql) throws Exception {
        return null;
    }

    @Override
    public List<Map<String, Object>> getAllDataWithDict(String datasourceId, String sql, Map<String, List<FieldDictVo>> dictMap) throws Exception {
        return null;
    }

    @Override
    public List<FieldDictVo> getDictData(String datasourceId, String dictSql, String key) throws Exception {
        return null;
    }

    @Override
    public Integer getDataCount(TBDatasourceConfig tbDatasourceConfig, String sql) throws Exception {
        return null;
    }

    @Override
    public Integer getDataCount(String id, String sql) throws Exception {
        return null;
    }

    @Override
    public List<FieldInfoVo> getAllFields(String datasourceId, String table) throws Exception {
        Optional<TBDatasourceConfig> byId = dataacRepository.findById(datasourceId);
        boolean isNotNull = byId.isPresent();
        if (isNotNull) {
//            TBDatasourceConfig tbDatasourceConfig = dataacRepository.getOne(datasourceId);
            TBDatasourceConfig tbDatasourceConfig = new TBDatasourceConfig();
            tbDatasourceConfig.setDatasourceType(DataSourceType.EXCEL);
            tbDatasourceConfig.setConnectionIp("192.168.16.171");
            tbDatasourceConfig.setConnectionPort(5432);
            tbDatasourceConfig.setSchemaDesc("postgres");
            tbDatasourceConfig.setDatasourceUserName("postgres");
            tbDatasourceConfig.setDatasourcePasswd("postgres");
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
