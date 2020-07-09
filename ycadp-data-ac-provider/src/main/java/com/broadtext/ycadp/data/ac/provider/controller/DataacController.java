package com.broadtext.ycadp.data.ac.provider.controller;

import com.broadtext.ycadp.base.entity.ListPager;
import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.core.common.listquery.Criteria;
import com.broadtext.ycadp.core.common.listquery.Restrictions;
import com.broadtext.ycadp.data.ac.api.annotation.EncryptMethod;
import com.broadtext.ycadp.data.ac.api.constants.DataSourceType;
import com.broadtext.ycadp.data.ac.api.entity.*;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.api.vo.*;
import com.broadtext.ycadp.data.ac.provider.service.*;
import com.broadtext.ycadp.data.ac.provider.service.authorization.AuthorizationService;
import com.broadtext.ycadp.data.ac.provider.utils.AesUtil;
import com.broadtext.ycadp.data.ac.provider.utils.ArrayUtil;
import com.broadtext.ycadp.data.ac.provider.utils.JDBCUtils;
import com.broadtext.ycadp.role.api.RoleApi;
import com.broadtext.ycadp.role.api.entity.Resource;
import com.broadtext.ycadp.util.userutil.CurrentUserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据接入Controller
 *
 * @author xuchenglong
 */
@RestController
@Slf4j
public class DataacController {
    @Autowired
    private DataacService dataacService;
    @Autowired
    private DataacGroupService dataacGroupService;
    @Autowired
    private DataacPackageService dataacPackageService;
    @Autowired
    private ExcelToolService excelToolService;
    @Autowired
    private DataExcelService dataExcelService;
    @Autowired
    private FileUploadOrDownloadService fileUploadOrDownloadService;
    @Autowired
    private ImportRecordService importRecordService;
    @Autowired
    private AuthorizationService authorizationService;
    @Value("${crypt.seckey}")
    private String secretKey;
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
    @Autowired
    private RoleApi roleApi;
    /**
     * 上传接口测试(建议这样实现)
     * @param multipartFile
     * @return
     */
    @PostMapping("/app/analyse/test")
    public String text(MultipartFile multipartFile){
        String fileKey = "";
        try {
            fileKey = fileUploadOrDownloadService.uploadSingleFile(multipartFile);
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }
        return fileKey;
    }

    /**
     * excel下载（fastdfs实现方式）
     * @param cloudUrl
     * @param response
     */
    @GetMapping("/data/datasource/downloadExcel")
    public void test(String cloudUrl, HttpServletResponse response){
        File file = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        String fileName = "测试.xlsx";
        try {
            file =  fileUploadOrDownloadService.downloadSingleFile(cloudUrl,fileName);
            //文件传回前台
            /* 第二步：根据已存在的文件，创建文件输入流 */
            inputStream = new BufferedInputStream(new FileInputStream(file));
            /* 第三步：创建缓冲区，大小为流的最大字符数 */
            byte[] buffer = new byte[inputStream.available()]; // int available() 返回值为流中尚未读取的字节的数量
            /* 第四步：从文件输入流读字节流到缓冲区 */
            if (buffer.length > 0) {
                inputStream.read(buffer);
            }

            fileName = file.getName();// 获取文件名
            response.reset();
            response.addHeader("Content-Disposition",
                    "attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            response.addHeader("Content-Length", "" + file.length());
            /* 第六步：创建文件输出流 */
            outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            /* 第七步：把缓冲区的内容写入文件输出流 */
            outputStream.write(buffer);
            /* 第八步：刷空输出流，并输出所有被缓存的字节 */
            outputStream.flush();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {

            /* 第九步：关闭输出流 */
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            /* 第十步：关闭输入流 */
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            /* 第十一步：删除本地文件 */
            if (file != null) {
                try {
                    FileUtils.deleteDirectory(file.getParentFile().getParentFile());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 校验重名
     * @param name
     * @param flag
     * @return
     */
    private String checkDataSourceName(String name, Integer flag) {
        List<TBDatasourceConfig> byName = dataacService.findByName(name);
        if (byName.size() > 0) {
            if (flag >= 1) {
                name = name.substring(0, name.length() - Integer.toString(flag).length() - 2);
            }
            flag += 1;
            name = name + "(" + Integer.toString(flag) + ")";
            return checkDataSourceName(name, flag);
        } else {
            return name;
        }
    }

    @GetMapping("/data/code")
    public RespEntity code(){
        try {
            String code = "AC_"+System.currentTimeMillis();
            Map<String, String> map = new ConcurrentHashMap<>();
            map.put("code",code);
            return new RespEntity(RespCode.SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }

    /**
     * 新增数据源
     *
     * @param datasourceConfig
     * @return
     */
    @PostMapping("/data/datasource")
    @EncryptMethod
    public RespEntity addDatasource(@RequestBody TBDatasourceConfigVo datasourceConfig) {
        RespEntity respEntity = null;
        TBDatasourceConfig dasource = new TBDatasourceConfig();
        dasource.setDatasourceName(checkDataSourceName(datasourceConfig.getDatasourceName(), 0));
        dasource.setDatasourceType(datasourceConfig.getDatasourceType());
        dasource.setConnectionIp(datasourceConfig.getConnectionIp());
        dasource.setConnectionPort(datasourceConfig.getConnectionPort());
        dasource.setDatasourceUserName(datasourceConfig.getDatasourceUserName());
        dasource.setDatasourcePasswd(datasourceConfig.getDatasourcePasswd());
        dasource.setDictSql(datasourceConfig.getDictSql());
        dasource.setRemark(datasourceConfig.getRemark());
        dasource.setSchemaDesc(datasourceConfig.getSchemaDesc());
        dasource.setPackageId(datasourceConfig.getPackageId());
        dasource.setCode(datasourceConfig.getCode());
        dasource.setDb2Schema(datasourceConfig.getDb2Schema());
        TBDatasourceConfig result = dataacService.addOne(dasource);
        if (result != null) {
//            datasourceConfig.setId(result.getId());
            respEntity = new RespEntity(RespCode.SUCCESS, result);
        } else {
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }

    /**
     * 添加excel数据源
     *
     * @param multipartFile
     * @param infoVo
     * @return
     */
    @PostMapping("/data/datasource/excel")
    public RespEntity addDatasourceExcel(@RequestParam("file") MultipartFile multipartFile, ExcelBaseInfoVo infoVo) {
        String fileKey = "";
        try {
            fileKey = fileUploadOrDownloadService.uploadSingleFile(multipartFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        String excelName = multipartFile.getOriginalFilename();
        Map<String, String> analysisMap = analysisExcel(multipartFile);
        if (analysisMap.containsKey("errorValue")) {
            System.out.println(analysisMap.get("errorValue"));
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, analysisMap.get("errorValue"));
        } else if (analysisMap.containsKey("exceptionValue")) {
            System.out.println("解析excel文件过程出现异常！！");
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "解析excel文件过程出现异常！！");
        }
        TBDatasourceConfig datasourceConfig = new TBDatasourceConfig();
        datasourceConfig.setDatasourceName(checkDataSourceName(infoVo.getDatasourceName(), 0))
                .setRemark(infoVo.getRemark())
                .setCloudUrl(fileKey)
                .setPackageId(infoVo.getPackageId())
                .setDatasourceType(DataSourceType.EXCEL).setCode(infoVo.getCode())
                .setSchemaDesc(excelName);
        TBDatasourceConfig dataConfigResult = dataacService.addOne(datasourceConfig);
        if (dataConfigResult.getId() != null) {
            TBImportRecord importRecord = new TBImportRecord();
            importRecord.setDatasourceId(dataConfigResult.getId())
                    .setFileName(excelName)
                    .setFileSize(String.valueOf(multipartFile.getSize()/1024))
                    .setCloudUrl(fileKey);
            importRecordService.addOne(importRecord);
            for (Map.Entry<String, String> entry : analysisMap.entrySet()) {
                TBDatasourceExcel excelEntity = new TBDatasourceExcel();
                excelEntity.setDatasourceId(dataConfigResult.getId())
                        .setSheetName(entry.getKey())
                        .setSheetTableName(entry.getValue());
                TBDatasourceExcel excelMappingResult = dataExcelService.addOne(excelEntity);
                if (excelMappingResult.getId() == null) {
                    return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "添加excel数据源映射关系实体失败！！");
                }
            }
            return new RespEntity(RespCode.SUCCESS, dataConfigResult);
        } else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "添加数据源实体失败！！");
        }
    }

    /**
     * excel中所有sheet表
     * @param id
     * @return
     */
    @GetMapping("/data/datasource/excelSheetList")
    public RespEntity excelSheet(String id) {
        List<TBDatasourceExcel> listByDataSourceId = dataExcelService.getListByDataSourceId(id);
        return new RespEntity(RespCode.SUCCESS, listByDataSourceId);
    }

    /**
     * excel表数据展示
     *
     * @param id
     * @param sheetName
     * @return
     */
    @GetMapping("/data/datasource/excelDataView")
    public RespEntity excelDataView(HttpServletRequest request, String id, String sheetName) {
//        List<Map<String,Object>> resultData = new LinkedList<Map<String,Object>>();
        Integer count = 0;
        String isPage = request.getParameter("isPage");
        String pageNum = request.getParameter("pageNum");
        String pageSize = request.getParameter("pageSize");
        Integer pSizes = pageSize == null || "".equals(pageSize) ? 1 : Integer.parseInt(pageSize);
        int pNum = (pageNum == null || "".equals(pageNum) ? 0 : Integer.parseInt(pageNum));
        int pSize = (pageSize == null || "".equals(pageSize) ? 0 : Integer.parseInt(pageSize));

        TBDatasourceExcel dSource = dataExcelService.findByIdAndSheetName(id, sheetName);
        String sheetTableName = dSource.getSheetTableName();
        PostgreConfigVo pVo = new PostgreConfigVo();
        pVo.setUrl("jdbc:postgresql://" + datasourceIpConfig + ":" + datasourcePort + "/" + schemaDesc)
                .setUser(datasourceUserName)
                .setPwd(datasourcePasswd);
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(pVo.getUrl(), pVo.getUser(), pVo.getPwd());
            connection.setAutoCommit(false);
            String sql = "select * from public." + sheetTableName;
            count = getDataCount(sql);
            if("true".equals(isPage)){
                sql += " limit " + pSize + " offset " + (pNum-1) * pSize;
            }
            ps = connection.prepareStatement("SELECT a.attname as \"字段名\",col_description(a.attrelid,a.attnum) as \"注释\",concat_ws('',t.typname,SUBSTRING(format_type(a.atttypid,a.atttypmod) from '\\(.*\\)')) as \"字段类型\" FROM pg_class as c,pg_attribute as a, pg_type as t WHERE c.relname = '"+sheetTableName+"' and a.atttypid = t.oid and a.attrelid = c.oid and a.attnum>0");
            rs = ps.executeQuery();
            Map<String, String> remarkMap = new ConcurrentHashMap<>();
            while (rs.next()) {
                String remarks = rs.getString(2);
                String fieldName = rs.getString(1);
                remarkMap.put(fieldName,remarks);
            }
            ps = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = ps.executeQuery();
            if (rs == null) {
                return new RespEntity(RespCode.SUCCESS);
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
                        rowData.put(md.getColumnLabel(i), rs.getObject(i));
                    }

                }
                list.add(rowData);
            }
            List<Map<String, Object>> newList = new LinkedList<>();
            for (Map<String, Object> map : list) {
                Map<String, Object> hashMap = new LinkedHashMap<>();
                for (String s : map.keySet()) {
                    if (remarkMap.containsKey(s)){
                        hashMap.put(remarkMap.get(s),map.get(s));
                    }
                }
                newList.add(hashMap);
            }
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("list", newList);
            map.put("total", count);
            map.put("pages", Math.ceil(count / pSizes));
            map.put("pageNum", pageNum);
            map.put("pageSize", pageSize);
            return new RespEntity(RespCode.SUCCESS, map);
        } catch (ClassNotFoundException e) {
            System.out.println("装载jdbc驱动失败");
            e.printStackTrace();
            try {
                connection.rollback();//回滚
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();//回滚
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, e.getMessage());
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取excel表大小
     * @param sql
     * @return
     */
    public int getDataCount(String sql) {
        PostgreConfigVo pVo = new PostgreConfigVo();
        pVo.setUrl("jdbc:postgresql://" + datasourceIpConfig + ":" + datasourcePort + "/" + schemaDesc)
                .setUser(datasourceUserName)
                .setPwd(datasourcePasswd);
        Connection connection;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(pVo.getUrl(), pVo.getUser(), pVo.getPwd());
            ps = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = ps.executeQuery();
            if (rs == null) {
                return 0;
            }
            rs.last();
            count = rs.getRow();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
        }
        return count;
    }

    /**
     * 删除数据源
     *
     * @param id
     * @return
     */
    @DeleteMapping("/data/datasource/{id}")
    public RespEntity deleteDatasource(@PathVariable("id") String id) {
        try {
            TBDatasourceConfig one = dataacService.getOne(id);
            if (one.getDatasourceType().equals(DataSourceType.EXCEL)) {
                List<TBDatasourceExcel> listByDataSourceId = dataExcelService.getListByDataSourceId(id);
                List<String> tableNameList = new ArrayList<>();
                for (TBDatasourceExcel e : listByDataSourceId) {
                    tableNameList.add(e.getSheetTableName());
                }
                String dropTableSql = "DROP TABLE ";
                for (int i = 0; i < tableNameList.size(); i++) {
                    dropTableSql += tableNameList.get(i) + ",";
                }
                dropTableSql = dropTableSql.substring(0, dropTableSql.length() - 1);
                PostgreConfigVo pVo = new PostgreConfigVo();
                pVo.setUrl("jdbc:postgresql://" + datasourceIpConfig + ":" + datasourcePort + "/" + schemaDesc)
                        .setUser(datasourceUserName)
                        .setPwd(datasourcePasswd);
                boolean b = excelToolService.generateDataInPostgre(pVo, dropTableSql);
                //再删除原有的映射关系和excel数据
                dataExcelService.deleteByDatasourceId(id);
            }
            dataacService.removeOne(id);
            importRecordService.deleteByDatasourceId(id);
            RespEntity respEntity = new RespEntity(RespCode.SUCCESS);
            return respEntity;
        } catch (Exception e) {
            e.printStackTrace();
            RespEntity respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE, e.getMessage());
            return respEntity;
        }
    }

    /**
     * 删除导入记录
     * @param id
     * @return
     */
    @DeleteMapping("/data/datasource/importrecord/{id}")
    public RespEntity deleteImportRecord(@PathVariable("id") String id) {
        boolean b = importRecordService.removeOne(id);
        if (b) {
            return new RespEntity(RespCode.SUCCESS);
        } else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }

    /**
     * 查询导入记录列表
     * @param excelVo
     * @param pager
     * @return
     */
    @GetMapping("/data/datasource/importrecord")
    public RespEntity getImportRecord(LinkedExcelVo excelVo, ListPager<TBImportRecord> pager) {
        RespEntity respEntity = null;
        Criteria<TBImportRecord> criteria = packCriteria(excelVo);
        try {
            if (StringUtils.isEmpty(pager.getSort())) {
                pager.setSort("createdTime,desc");
            }
            ListPager<TBImportRecord> resultPager = new ListPager<>();
            if ("false".equals(excelVo.getIsPage())) {
                //不分页
                List<TBImportRecord> listOrderByTime = importRecordService.getListByFilter(criteria, pager.getSort());
                resultPager.setList(listOrderByTime);
                resultPager.setPages(listOrderByTime.size());
                resultPager.setPageSize(listOrderByTime.size());
                resultPager.setPageNum(1);
                respEntity = new RespEntity(RespCode.SUCCESS, resultPager);
            } else {
                //分页
                pager = importRecordService.getListByFilter(criteria, pager);
                List<TBImportRecord> list = pager.getList();
//                List<TBImportRecord> list = importRecordService.getListOrderByTime(excelVo.getDatasourceId());
                resultPager.setList(list);
                resultPager.setPages(pager.getPages());
                resultPager.setPageSize(pager.getPageSize());
                resultPager.setPageNum(pager.getPageNum());
                resultPager.setTotal(pager.getTotal());
                respEntity = new RespEntity(RespCode.SUCCESS, resultPager);
            }
        } catch (Exception e) {
            e.printStackTrace();
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE, e.getMessage());
        }
        return respEntity;
    }

    private Criteria<TBImportRecord> packCriteria(LinkedExcelVo excelVo) {
        //分页查询
        Criteria<TBImportRecord> c = new Criteria<>();
        try {
            //封装分页条件 Restrictions  Criteria  为自己封装类，非hibernate类
            if (StringUtils.isNotEmpty(excelVo.getFileName())) {
                c.add(Restrictions.like("fileName", excelVo.getFileName(), true));
            }
            if (StringUtils.isNotEmpty(excelVo.getDatasourceId())) {
                c.add(Restrictions.like("datasourceId", excelVo.getDatasourceId(), true));
            }
            if (StringUtils.isNotEmpty(excelVo.getCreatedName())) {
                c.add(Restrictions.like("createdName", excelVo.getCreatedName(), true));
            }
            if (excelVo.getCreatedTime() != null && !"".equals(excelVo.getCreatedTime())) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (excelVo.getCreatedTime().split(",")[0] != null && !"".equals(excelVo.getCreatedTime().split(",")[0])) {
                    Date startTime = formatter.parse(excelVo.getCreatedTime().split(",")[0]);
                    c.add(Restrictions.gte("createdTime", startTime, true));
                }
                if (excelVo.getCreatedTime().split(",")[1] != null && !"".equals(excelVo.getCreatedTime().split(",")[1])) {
                    Date endTime = formatter.parse(excelVo.getCreatedTime().split(",")[1]);
                    c.add(Restrictions.lte("createdTime", endTime, true));
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return c;
    }

    /**
     * 查询数据源列表
     *
     * @param request
     * @param pager
     * @return
     */
    @GetMapping("/data/datasource1")
    public RespEntity getDatasources(HttpServletRequest request, ListPager<DataSourceListVo> pager) {
        RespEntity respEntity = null;
        String ispage = request.getParameter("isPage");
        String datasourceName = request.getParameter("datasourceName");
        List<TBDatasourceConfig> datas = new ArrayList<>();
        if (datasourceName == null) {//无筛选条件查询所有
            datas = dataacService.getList();
        } else if (!"".equals(datasourceName)) {//有筛选条件
            datas = dataacService.getListByDatasourceName(datasourceName);
        } else {//无筛选条件查询所有
            datas = dataacService.getList();
        }
        List<DataSourceListVo> voDatas = new ArrayList<>();
        for (TBDatasourceConfig daSource : datas) {
            DataSourceListVo vo = new DataSourceListVo();
            vo.setId(daSource.getId());
            vo.setDatasourceName(daSource.getDatasourceName());
            vo.setDatasourceType(daSource.getDatasourceType());
            voDatas.add(vo);
        }
        if (ispage == null || "true".equals(ispage)) {
            //分页
            String sort = request.getParameter("sort");
            String pageNum = request.getParameter("pageNum");
            String pageSize = request.getParameter("pageSize");
            if (pageNum == null || pageSize == null || "".equals(pageNum) || "".equals(pageSize)) {
                respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "请求分页参数有误");
            } else {
                pager.setList(voDatas);
                pager.setPageNum(Integer.parseInt(pageNum));
                pager.setPageSize(Integer.parseInt(pageSize));
                pager.setTotal(voDatas.size());
                if (sort != null || !"".equals(sort)) {
                    pager.setSort(sort);
                }
                respEntity = new RespEntity(RespCode.SUCCESS, pager);
            }
        } else {
            //不分页
            pager.setList(voDatas);
            pager.setPageNum(1);
            pager.setTotal(voDatas.size());//所有行记录数
            respEntity = new RespEntity(RespCode.SUCCESS, pager);
        }
        return respEntity;
    }

    /**
     * 查询数据源的三级结构
     * @return
     */
    @GetMapping("/data/treeDatasource")
    public RespEntity getTreeDataResources(){
        String userId = CurrentUserUtils.getUser().getUserId();
//        String userId = "8a8080916d43ec07016d5d74da9a0110";
        List<TBPermitPolicy> permitPolicyByName = authorizationService.findPermitPolicyByName("管理员", "编辑者");
        List<String> permitIdList = new ArrayList<>();
        for (TBPermitPolicy p : permitPolicyByName) {
            permitIdList.add(p.getId());
        }
        List<TBAclDetail> resList = new ArrayList<>();
        for (String s : permitIdList) {
            resList.addAll(authorizationService.findByModulePermitUser("dataac", s, userId));
        }
        if (resList.size() < 1) {
            return new RespEntity(RespCode.SUCCESS, new ArrayList<>());
        } else {
            List<String> groupIdList = new ArrayList<>();
            for (TBAclDetail d : resList) {
                groupIdList.add(d.getGroupId());
            }
            List<TreeGroupVo> resultList = new ArrayList<>();
            List<TBDatasourceGroup> groups = dataacGroupService.getListBySortNum();
            List<TBDatasourceGroup> realGroupList = new ArrayList<>();
            for (TBDatasourceGroup t : groups) {
                if (groupIdList.contains(t.getId())) realGroupList.add(t);
            }
            for (TBDatasourceGroup g : realGroupList) {
                List<TBDatasourcePackage> packages = dataacPackageService.getOrderedListByGroupId(g.getId());
                TreeGroupVo groupVo = new TreeGroupVo();
                List<TreePackageVo> packageVoList = new ArrayList<>();
                for (TBDatasourcePackage p : packages) {
                    List<TBDatasourceConfig> datasources = dataacService.getDatasourceByPackageId(p.getId());
                    TreePackageVo packageVo = new TreePackageVo();
                    List<DataSourceListVo> dataSourceListVos = datasourceToDatasourceVo(datasources);
                    packageVo.setId(p.getId());
                    packageVo.setGroupId(p.getGroupId());
                    packageVo.setPackageName(p.getPackageName());
                    packageVo.setSortNum(p.getSortNum());
                    packageVo.setDataSourceVoList(dataSourceListVos);
                    packageVoList.add(packageVo);
                }
                groupVo.setId(g.getId());
                groupVo.setGroupName(g.getGroupName());
                groupVo.setSortNum(g.getSortNum());
                groupVo.setPackageVoList(packageVoList);
                resultList.add(groupVo);
            }
            return new RespEntity(RespCode.SUCCESS, resultList);
        }
    }

    /**
     * entity转vo
     * @param datasources
     * @return
     */
    private List<DataSourceListVo> datasourceToDatasourceVo(List<TBDatasourceConfig> datasources) {
        List<DataSourceListVo> resultList = new ArrayList<>();
        for (TBDatasourceConfig t : datasources) {
            DataSourceListVo vo = new DataSourceListVo();
            vo.setId(t.getId());
            vo.setDatasourceName(t.getDatasourceName());
            vo.setDatasourceType(t.getDatasourceType());
            resultList.add(vo);
        }
        return resultList;
    }

    /**
     * 查询数据源列表
     *
     * @param packageId
     * @return
     */
    @GetMapping("/data/datasource")
    public RespEntity getDatasources(String packageId) {
        RespEntity respEntity = null;
        try {
            List<DataSourceListVo> list = new ArrayList<>();
            Map<String, List<DataSourceListVo>> map = new HashMap<>();
            if (packageId != null && !"".equals(packageId)) {
                List<TBDatasourceConfig> dataSourceList = dataacService.getDatasourceByPackageId(packageId);
                for (TBDatasourceConfig tbDatasourceConfig : dataSourceList) {
                    DataSourceListVo dataSourceListVo = new DataSourceListVo();
                    dataSourceListVo.setId(tbDatasourceConfig.getId());
                    dataSourceListVo.setDatasourceName(tbDatasourceConfig.getDatasourceName());
                    dataSourceListVo.setDatasourceType(tbDatasourceConfig.getDatasourceType());
                    dataSourceListVo.setConnectionIp(tbDatasourceConfig.getConnectionIp());
                    dataSourceListVo.setConnectionPort(tbDatasourceConfig.getConnectionPort());
                    dataSourceListVo.setSchemaDesc(tbDatasourceConfig.getSchemaDesc());
                    dataSourceListVo.setPackageId(tbDatasourceConfig.getPackageId());
                    dataSourceListVo.setCreatedTime(tbDatasourceConfig.getCreatedTime());
                    list.add(dataSourceListVo);
                }
                list.sort((o1, o2) -> {
                    Date createdTime1 = o1.getCreatedTime();
                    Date createdTime2 = o2.getCreatedTime();
                    return createdTime2.compareTo(createdTime1);
                });
                map.put("list", list);
                respEntity = new RespEntity<>(RespCode.SUCCESS, map);
            } else {
                map.put("list", list);
                respEntity = new RespEntity<>(RespCode.SUCCESS, map);
            }
        } catch (Exception e) {
            respEntity = new RespEntity<>(DataacRespCode.DATAAC_RESP_CODE, "系统出错");
        }
        return respEntity;
    }


    /**
     * 查询数据源明细信息
     *
     * @param id
     * @return
     */
    @GetMapping("/data/datasource/{id}")
    public RespEntity getDatasource(@PathVariable("id") String id) {
        try {
            TBDatasourceConfig datasource = dataacService.findById(id);
            RespEntity respEntity = null;
            if (datasource != null) {
                datasource.setDatasourcePasswd(AesUtil.decrypt(datasource.getDatasourcePasswd(),secretKey));
                respEntity = new RespEntity(RespCode.SUCCESS, datasource);
            } else {
                respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
            }
            return respEntity;
        } catch (Exception e) {
            e.printStackTrace();
            RespEntity respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE, e.getMessage());
            return respEntity;
        }
    }

    /**
     * 编辑数据源
     *
     * @param id
     * @param datasourceConfig
     * @return
     */
    @PutMapping("/data/datasource/{id}")
    public RespEntity updateDatasource(@RequestBody TBDatasourceConfigVo datasourceConfig,@PathVariable("id") String id) {
        RespEntity respEntity = null;
        TBDatasourceConfig dasource = dataacService.findById(id);
        if (dasource != null) {
            dasource.setDatasourceName(datasourceConfig.getDatasourceName());
            dasource.setDatasourceType(datasourceConfig.getDatasourceType());
            dasource.setConnectionIp(datasourceConfig.getConnectionIp());
            dasource.setConnectionPort(datasourceConfig.getConnectionPort());
            dasource.setDatasourceUserName(datasourceConfig.getDatasourceUserName());
            if (datasourceConfig.getDatasourcePasswd().equals(dasource.getDatasourcePasswd())) {
                dasource.setDatasourcePasswd(datasourceConfig.getDatasourcePasswd());
            } else {
                dasource.setDatasourcePasswd(AesUtil.encrypt(datasourceConfig.getDatasourcePasswd(),secretKey));
            }
            if (StringUtils.isEmpty(datasourceConfig.getCode())){
                dasource.setCode("AC_"+System.currentTimeMillis());
            }
            dasource.setDictSql(datasourceConfig.getDictSql());
            dasource.setRemark(datasourceConfig.getRemark());
            dasource.setSchemaDesc(datasourceConfig.getSchemaDesc());
            dasource.setDb2Schema(datasourceConfig.getDb2Schema());
            dasource.setPackageId(datasourceConfig.getPackageId());
            dataacService.updateOne(dasource);
            datasourceConfig.setId(id);
            respEntity = new RespEntity(RespCode.SUCCESS, datasourceConfig);
        } else {
            respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }

    /**
     * 编辑excel数据源
     *
     * @param multipartFile
     * @param infoVo
     * @return
     */
    @PostMapping("/data/datasource/excel/edit")
    public RespEntity updateDatasourceExcel(@RequestParam("file") MultipartFile multipartFile, ExcelBaseInfoVo infoVo) {
        String id = infoVo.getId();
        TBDatasourceConfig dasource = dataacService.findById(id);
        if (infoVo.getFlag().equals("1")) {//生成过资产
            dasource.setDatasourceName(infoVo.getDatasourceName())
                    .setRemark(infoVo.getRemark())
                    .setPackageId(infoVo.getPackageId());
            TBDatasourceConfig datasourceConfig = dataacService.updateOne(dasource);
            return new RespEntity(RespCode.SUCCESS, datasourceConfig);
        } else {//未生成过资产，允许修改excel文件
            if (multipartFile.isEmpty()) {
                dasource.setDatasourceName(infoVo.getDatasourceName())
                        .setRemark(infoVo.getRemark())
                        .setPackageId(infoVo.getPackageId());
                TBDatasourceConfig datasourceConfig = dataacService.updateOne(dasource);
                return new RespEntity(RespCode.SUCCESS, datasourceConfig);
            } else {
                //上传新excel文件到fastdfs
                String fileKey = "";
                try {
                    fileKey = fileUploadOrDownloadService.uploadSingleFile(multipartFile);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (MyException e) {
                    e.printStackTrace();
                }
                //先删除pg数据库中的excel表数据
                List<TBDatasourceExcel> listByDataSourceId = dataExcelService.getListByDataSourceId(id);
                List<String> tableNameList = new ArrayList<>();
                for (TBDatasourceExcel e : listByDataSourceId) {
                    tableNameList.add(e.getSheetTableName());
                }
                String dropTableSql = "DROP TABLE ";
                for (int i = 0; i < tableNameList.size(); i++) {
                    dropTableSql += tableNameList.get(i) + ",";
                }
                dropTableSql = dropTableSql.substring(0, dropTableSql.length() - 1);
                PostgreConfigVo pVo = new PostgreConfigVo();
                pVo.setUrl("jdbc:postgresql://" + datasourceIpConfig + ":" + datasourcePort + "/" + schemaDesc)
                        .setUser(datasourceUserName)
                        .setPwd(datasourcePasswd);
                boolean b = excelToolService.generateDataInPostgre(pVo, dropTableSql);
                //再删除原有的映射关系和excel数据
                dataExcelService.deleteByDatasourceId(id);
                //然后添加各项数据
                String excelName = multipartFile.getOriginalFilename();
                Map<String, String> analysisMap = analysisExcel(multipartFile);
                if (analysisMap.containsKey("errorValue")) {
                    System.out.println(analysisMap.get("errorValue"));
                    return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, analysisMap.get("errorValue"));
                } else if (analysisMap.containsKey("exceptionValue")) {
                    System.out.println("解析excel文件过程出现异常！！");
                    return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "解析excel文件过程出现异常！表头存在空数据或单元格数据类型不匹配！");
                }
                dasource.setDatasourceName(infoVo.getDatasourceName())
                        .setRemark(infoVo.getRemark())
                        .setPackageId(infoVo.getPackageId())
                        .setCloudUrl(fileKey)
                        .setSchemaDesc(excelName);
                TBDatasourceConfig dataConfigResult = dataacService.updateOne(dasource);
                if (dataConfigResult.getId() != null) {
                    for (Map.Entry<String, String> entry : analysisMap.entrySet()) {
                        TBDatasourceExcel excelEntity = new TBDatasourceExcel();
                        excelEntity.setDatasourceId(dataConfigResult.getId())
                                .setSheetName(entry.getKey())
                                .setSheetTableName(entry.getValue());
                        TBDatasourceExcel excelMappingResult = dataExcelService.addOne(excelEntity);
                        if (excelMappingResult.getId() == null) {
                            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "添加excel数据源映射关系实体失败！！");
                        }
                    }
                    return new RespEntity(RespCode.SUCCESS, dataConfigResult);
                } else {
                    return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "编辑数据源实体失败！！");
                }
            }

        }
    }

    /**
     * excel导入（全量更新）
     * @param id
     * @param multipartFile
     * @return
     */
    @PostMapping("/data/datasource/excel/import/{id}")
    public RespEntity excelImport(@PathVariable("id") String id, @RequestParam("file") MultipartFile multipartFile) {
        List<String> sheetNamesFromDb = new ArrayList<>();
        List<TBDatasourceExcel> listByDataSourceId = dataExcelService.getListByDataSourceId(id);
        for (TBDatasourceExcel e : listByDataSourceId) {
            sheetNamesFromDb.add(e.getSheetName());
        }
        int size = sheetNamesFromDb.size();
        int compareNum = 0;
        List<String> excelSheetNames = getExcelSheetNames(multipartFile);
        for (int i = 0; i < excelSheetNames.size(); i++) {
            for (int j = 0; j < sheetNamesFromDb.size(); j++) {
                if (excelSheetNames.get(i).equals(sheetNamesFromDb.get(j))) {
                    compareNum++;
                    break;
                }
                continue;
            }
        }
        if (compareNum == size) {//校验sheet名称无误，开始全量更新
            //先清空pg数据库中的excel表数据
            List<String> tableNameList = new ArrayList<>();
            for (TBDatasourceExcel e : listByDataSourceId) {
                tableNameList.add(e.getSheetTableName());
            }
            String cleanTableSql = "TRUNCATE TABLE ";
            for (int i = 0; i < tableNameList.size(); i++) {
                cleanTableSql += tableNameList.get(i) + ",";
            }
            cleanTableSql = cleanTableSql.substring(0, cleanTableSql.length() - 1);
            PostgreConfigVo pVo = new PostgreConfigVo();
            pVo.setUrl("jdbc:postgresql://" + datasourceIpConfig + ":" + datasourcePort + "/" + schemaDesc)
                    .setUser(datasourceUserName)
                    .setPwd(datasourcePasswd);
            boolean b = excelToolService.generateDataInPostgre(pVo, cleanTableSql);
//            //再删除原有的映射关系和excel数据
//            dataExcelService.deleteByDatasourceId(id);
            //然后添加各项数据
            String excelName = multipartFile.getOriginalFilename();
//            Map<String, String> analysisMap = analysisExcel(multipartFile);
            Map<String, String> analysisMap = analysisExcelUpdate(multipartFile,id);
            if (analysisMap.containsKey("errorValue")) {
                System.out.println(analysisMap.get("errorValue"));
                return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, analysisMap.get("errorValue"));
            } else if (analysisMap.containsKey("exceptionValue")) {
                System.out.println("解析excel文件过程出现异常！！");
                return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "解析excel文件过程出现异常！!");
            }
            //上传新excel文件到fastdfs
            String fileKey = "";
            try {
                fileKey = fileUploadOrDownloadService.uploadSingleFile(multipartFile);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MyException e) {
                e.printStackTrace();
            }
            TBDatasourceConfig dasource = dataacService.findById(id);
            dasource.setCloudUrl(fileKey);
            dataacService.updateOne(dasource);
            TBImportRecord importRecord = new TBImportRecord();
            importRecord.setDatasourceId(id)
                    .setFileName(excelName)
                    .setFileSize(String.valueOf(multipartFile.getSize()/1024))
                    .setCloudUrl(fileKey);
            importRecordService.addOne(importRecord);
            return new RespEntity(RespCode.SUCCESS);
        } else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "sheet名称不允许发生变化，请检查");
        }
    }

    /**
     * 新增group
     *
     * @param groupVo
     * @return
     */
    @PostMapping("/data/datasource/group")
    public RespEntity addGroup(@RequestBody GroupVo groupVo) {
        TBDatasourceGroup groupEntity = new TBDatasourceGroup();
        groupEntity.setGroupName(groupVo.getGroupName());
        int maxSortNum = 0;
        List<TBDatasourceGroup> list = dataacGroupService.getList();
        for (TBDatasourceGroup group : list) {
            int tempSortNum = Integer.valueOf(group.getSortNum());
            if (tempSortNum > maxSortNum) {
                maxSortNum = tempSortNum;
            }
        }
        groupEntity.setSortNum(String.valueOf(++maxSortNum));
//        groupEntity.setSortNum(groupVo.getSortNum());
        TBDatasourceGroup tbDatasourceGroup = dataacGroupService.addOne(groupEntity);
        if (tbDatasourceGroup != null) {
            return new RespEntity(RespCode.SUCCESS, tbDatasourceGroup);
        } else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }

    /**
     * 编辑group
     *
     * @param id
     * @param groupVo
     * @return
     */
    @PutMapping("/data/datasource/group/{id}")
    public RespEntity editGroup(@PathVariable("id") String id, @RequestBody GroupVo groupVo) {
        TBDatasourceGroup byId = dataacGroupService.findById(id);
        byId.setGroupName(groupVo.getGroupName());
//        byId.setSortNum(groupVo.getSortNum());
        TBDatasourceGroup tbDatasourceGroup = dataacGroupService.updateOne(byId);
        if (tbDatasourceGroup != null) {
            return new RespEntity(RespCode.SUCCESS, tbDatasourceGroup);
        } else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }

    /**
     * 回显group
     *
     * @param id
     * @return
     */
    @GetMapping("/data/datasource/group/{id}")
    public RespEntity echoGroup(@PathVariable("id") String id) {
        TBDatasourceGroup byId = dataacGroupService.findById(id);
        return new RespEntity(RespCode.SUCCESS, byId);
    }

    /**
     * 删除group
     *
     * @param id
     * @return
     */
    @DeleteMapping("/data/datasource/group/{id}")
    public RespEntity deleteGroup(@PathVariable("id") String id) {
        String sortNum = dataacGroupService.getOne(id).getSortNum();
        int sortNumInt = Integer.valueOf(sortNum);
        List<TBDatasourceGroup> list = dataacGroupService.getList();
        for (TBDatasourceGroup group : list) {
            int tempSortNum = Integer.valueOf(group.getSortNum());
            if (tempSortNum > sortNumInt) {
                group.setSortNum(String.valueOf(--tempSortNum));
                dataacGroupService.updateOne(group);
            }
        }
        dataacGroupService.removeOne(id);
        dataacPackageService.removePackageByGroupId(id);
        return new RespEntity(RespCode.SUCCESS);
    }

    /**
     * 新增package
     *
     * @param packageAddVo
     * @return
     */
    @PostMapping("/data/datasource/package")
    public RespEntity addPackage(@RequestBody PackageAddVo packageAddVo) {
        TBDatasourcePackage tbDatasourcePackage = new TBDatasourcePackage();
        tbDatasourcePackage.setGroupId(packageAddVo.getGroupId());
        tbDatasourcePackage.setPackageName(packageAddVo.getPackageName());
        int maxSortNum = 0;
        List<TBDatasourcePackage> listByGroupId = dataacPackageService.getOrderedListByGroupId(packageAddVo.getGroupId());
        for (TBDatasourcePackage p : listByGroupId) {
            int tempSortNum = Integer.valueOf(p.getSortNum());
            if (tempSortNum > maxSortNum) {
                maxSortNum = tempSortNum;
            }
        }
        tbDatasourcePackage.setSortNum(String.valueOf(++maxSortNum));
//        tbDatasourcePackage.setSortNum(packageAddVo.getSortNum());
        TBDatasourcePackage result = dataacPackageService.addOne(tbDatasourcePackage);
        if (result != null) {
            return new RespEntity(RespCode.SUCCESS, result);
        } else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }

    }

    /**
     * 编辑package
     *
     * @param id
     * @param packageAddVo
     * @return
     */
    @PutMapping("/data/datasource/package/{id}")
    public RespEntity editPackage(@PathVariable("id") String id, @RequestBody PackageAddVo packageAddVo) {
        TBDatasourcePackage byId = dataacPackageService.findById(id);
        byId.setPackageName(packageAddVo.getPackageName());
//        byId.setSortNum(packageAddVo.getSortNum());
        TBDatasourcePackage tbDatasourcePackage = dataacPackageService.updateOne(byId);
        if (tbDatasourcePackage != null) {
            return new RespEntity(RespCode.SUCCESS, tbDatasourcePackage);
        } else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }

    /**
     * 回显package
     *
     * @param id
     * @return
     */
    @GetMapping("/data/datasource/package/{id}")
    public RespEntity echoPackage(@PathVariable("id") String id) {
        TBDatasourcePackage byId = dataacPackageService.findById(id);
        return new RespEntity(RespCode.SUCCESS, byId);
    }

    /**
     * 删除package
     *
     * @param id
     * @return
     */
    @DeleteMapping("/data/datasource/package/{id}")
    public RespEntity deletePackage(@PathVariable("id") String id) {
        TBDatasourcePackage one = dataacPackageService.getOne(id);
        int sortNum = Integer.valueOf(one.getSortNum());
        List<TBDatasourcePackage> listByGroupId = dataacPackageService.getOrderedListByGroupId(one.getGroupId());
        for (TBDatasourcePackage p : listByGroupId) {
            int tempSortNum = Integer.valueOf(p.getSortNum());
            if (tempSortNum > sortNum) {
                p.setSortNum(String.valueOf(--tempSortNum));
                dataacPackageService.updateOne(p);
            }
        }
        dataacPackageService.removeOne(id);
        return new RespEntity(RespCode.SUCCESS);
    }

    /**
     * 包结构树形展示（完整树形）
     *
     * @return
     */
    @GetMapping("/data/datasource/tree")
    public RespEntity viewTree() {
        String userId = CurrentUserUtils.getUser().getUserId();
//        String userId = "8a8080916d43ec07016d5d74da9a0110";
        RespEntity<List<Resource>> byUserId = roleApi.findByUserId(userId);
        List<Resource> data = byUserId.getData();
        boolean viewAllGroupAuth = false;
        for (Resource r : data) {
            if (r.getResourceCode().equals("data.access.view")) {
                viewAllGroupAuth = true;
                break;
            }
        }
        List<TBPermitPolicy> permitPolicyByName = authorizationService.findPermitPolicyByName("管理员", "编辑者");
        List<String> permitIdList = new ArrayList<>();
        for (TBPermitPolicy p : permitPolicyByName) {
            permitIdList.add(p.getId());
        }
        List<TBAclDetail> resList = new ArrayList<>();
        for (String s : permitIdList) {
            resList.addAll(authorizationService.findByModulePermitUser("dataac", s, userId));
        }
        List<String> groupIdList = new ArrayList<>();
        for (TBAclDetail d : resList) {
            groupIdList.add(d.getGroupId());
        }
        List<GroupVo> groupVoList = new ArrayList<>();
        List<TBDatasourceGroup> groupList = dataacGroupService.getListBySortNum();//排序过的组List
        List<TBDatasourceGroup> realGroupList = new ArrayList<>();
        List<TBDatasourcePackage> packageList;
        if (viewAllGroupAuth) {
            for (TBDatasourceGroup g : groupList) {
                GroupVo gVo = new GroupVo();
                gVo.setId(g.getId());
                gVo.setGroupName(g.getGroupName());
                gVo.setSortNum(g.getSortNum());
                List<PackageVo> packageVoList = new ArrayList<>();
                packageList = dataacPackageService.getOrderedListByGroupId(g.getId());
                for (TBDatasourcePackage p : packageList) {
                    PackageVo pVo = new PackageVo();
                    pVo.setId(p.getId());
                    pVo.setGroupId(g.getId());
                    pVo.setPackageName(p.getPackageName());
                    pVo.setSortNum(p.getSortNum());
                    packageVoList.add(pVo);
                }
                gVo.setPackageVoList(packageVoList);
                groupVoList.add(gVo);
            }
            return new RespEntity(RespCode.SUCCESS, groupVoList);
        }else {
            if (resList.size() < 1) {
                return new RespEntity(RespCode.SUCCESS, new ArrayList<>());
            } else {
                for (TBDatasourceGroup t : groupList) {
                    if (groupIdList.contains(t.getId())) realGroupList.add(t);
                }
                for (TBDatasourceGroup g : realGroupList) {
                    GroupVo gVo = new GroupVo();
                    gVo.setId(g.getId());
                    gVo.setGroupName(g.getGroupName());
                    gVo.setSortNum(g.getSortNum());
                    List<PackageVo> packageVoList = new ArrayList<>();
                    packageList = dataacPackageService.getOrderedListByGroupId(g.getId());
                    for (TBDatasourcePackage p : packageList) {
                        PackageVo pVo = new PackageVo();
                        pVo.setId(p.getId());
                        pVo.setGroupId(g.getId());
                        pVo.setPackageName(p.getPackageName());
                        pVo.setSortNum(p.getSortNum());
                        packageVoList.add(pVo);
                    }
                    gVo.setPackageVoList(packageVoList);
                    groupVoList.add(gVo);
                }
                return new RespEntity(RespCode.SUCCESS, groupVoList);
            }
        }
    }

    /**
     * 移动改变顺序
     *
     * @param sortGroupVo
     * @return
     */
    @PostMapping("/data/datasource/sort")
    public RespEntity changeSort(@RequestBody SortGroupVo sortGroupVo) {
        List<GroupVo> groupVoList = sortGroupVo.getGroupVoList();
        for (GroupVo gVo : groupVoList) {
            TBDatasourceGroup theGroup = dataacGroupService.getOne(gVo.getId());//该vo对应的真实group实体
            theGroup.setSortNum(gVo.getSortNum());
            dataacGroupService.updateOne(theGroup);
            List<PackageVo> packageVoList = gVo.getPackageVoList();
            for (PackageVo pVo : packageVoList) {
                TBDatasourcePackage thePackage = dataacPackageService.getOne(pVo.getId());
                thePackage.setGroupId(pVo.getGroupId());
                thePackage.setSortNum(pVo.getSortNum());
                dataacPackageService.updateOne(thePackage);
            }
        }
        return new RespEntity(RespCode.SUCCESS);
    }

    /**
     * 拖动包权限校验
     * @param currentGroupId
     * @param targetGroupId
     * @return
     */
    @GetMapping("/data/datasource/sort/judge")
    public RespEntity judgeSortingAuth(String currentGroupId, String targetGroupId) {
        PackageSortJudgeVo judgeVo = new PackageSortJudgeVo();
        String userId = CurrentUserUtils.getUser().getUserId();
//        String userId = "8a8080916d43ec07016d5d74da9a0110";
        List<TBPermitPolicy> permitPolicyByName = authorizationService.getList();
        String editPolicyId = "";
        String adminPolicyId = "";
        for (TBPermitPolicy p : permitPolicyByName) {
            if (p.getName().equals("管理员")) {
                adminPolicyId = p.getId();
            } else if (p.getName().equals("编辑者")) {
                editPolicyId = p.getId();
            }
        }
        List<TBAclDetail> resList = new ArrayList<>();
        if (currentGroupId.equals(targetGroupId)) {
            resList = authorizationService.findByModuleGroupIdUser("dataac", currentGroupId, userId);
            judgeVo.setMoveType("0");
            if (resList.isEmpty()) {
                judgeVo.setAuthFlag(false);
            } else {
                judgeVo.setAuthFlag(true);
            }
        } else {
            judgeVo.setMoveType("1");
            resList = authorizationService.findByModuleGroupIdUser("dataac", currentGroupId, userId);
            List<String> currentGroupAuthIds = new ArrayList<>();
            for (TBAclDetail d : resList) {
                currentGroupAuthIds.add(d.getPermitPolicyId());
            }
            if (currentGroupAuthIds.contains(adminPolicyId) || currentGroupAuthIds.contains(editPolicyId)) {
                judgeVo.setAuthFlag(true);
            } else {
                judgeVo.setAuthFlag(false);
            }
        }
        return new RespEntity(RespCode.SUCCESS, judgeVo);
    }

    /**
     * 拖动包权限校验（开放给其他模块）
     * @param currentGroupId
     * @param targetGroupId
     * @param moduleName
     * @return
     */
    @GetMapping("/data/datasource/sort/judgeForOtherModule")
    public RespEntity judgeSortingAuthForOtherModule(String currentGroupId, String targetGroupId, String moduleName) {
        PackageSortJudgeVo judgeVo = new PackageSortJudgeVo();
        String userId = CurrentUserUtils.getUser().getUserId();
//        String userId = "8a8080916d43ec07016d5d74da9a0110";
        List<TBPermitPolicy> permitPolicyByName = authorizationService.getList();
        String editPolicyId = "";
        String adminPolicyId = "";
        for (TBPermitPolicy p : permitPolicyByName) {
            if (p.getName().equals("管理员")) {
                adminPolicyId = p.getId();
            } else if (p.getName().equals("编辑者")) {
                editPolicyId = p.getId();
            }
        }
        List<TBAclDetail> resList = new ArrayList<>();
        if (currentGroupId.equals(targetGroupId)) {
            resList = authorizationService.findByModuleGroupIdUser(moduleName, currentGroupId, userId);
            judgeVo.setMoveType("0");
            if (resList.isEmpty()) {
                judgeVo.setAuthFlag(false);
            } else {
                judgeVo.setAuthFlag(true);
            }
        } else {
            judgeVo.setMoveType("1");
            resList = authorizationService.findByModuleGroupIdUser(moduleName, currentGroupId, userId);
            List<String> currentGroupAuthIds = new ArrayList<>();
            for (TBAclDetail d : resList) {
                currentGroupAuthIds.add(d.getPermitPolicyId());
            }
            if (currentGroupAuthIds.contains(adminPolicyId) || currentGroupAuthIds.contains(editPolicyId)) {
                judgeVo.setAuthFlag(true);
            } else {
                judgeVo.setAuthFlag(false);
            }
        }
        return new RespEntity(RespCode.SUCCESS, judgeVo);
    }

    /**
     * 开放给其他项目 获取有权限的组id列表
     * @param moduleName
     * @param userId
     * @return
     */
    @GetMapping("/data/datasource/treeForOtherProject")
    public RespEntity<List<String>> getGrantedGroups(String moduleName, String userId) {
        List<TBPermitPolicy> permitPolicyByName = authorizationService.findPermitPolicyByName("管理员", "编辑者");
        List<String> permitIdList = new ArrayList<>();
        for (TBPermitPolicy p : permitPolicyByName) {
            permitIdList.add(p.getId());
        }
        List<TBAclDetail> resList = new ArrayList<>();
        for (String s : permitIdList) {
            resList.addAll(authorizationService.findByModulePermitUser(moduleName, s, userId));
        }
        if (resList.size() < 1) {
            return new RespEntity(RespCode.SUCCESS, new ArrayList<>());
        } else {
            List<String> groupIdList = new ArrayList<>();
            for (TBAclDetail d : resList) {
                groupIdList.add(d.getGroupId());
            }
            return new RespEntity<>(RespCode.SUCCESS, groupIdList);
        }
    }


    /**
     * 解析Excel文件
     * 1.将其真实数据放入postgres数据库中，每个sheet对应一张表
     *
     * ----返回各个sheet的名称list，用在在ac库的映射表中
     *
     * @param multipartFile
     * @return
     */
    public Map<String, String> analysisExcel(MultipartFile multipartFile) {
        //配置我们的postgreSql数据库链接信息
        PostgreConfigVo pVo = new PostgreConfigVo();
        pVo.setUrl("jdbc:postgresql://" + datasourceIpConfig + ":" + datasourcePort + "/" + schemaDesc)
                .setUser(datasourceUserName)
                .setPwd(datasourcePasswd);
        //开始处理MultipartFile
        File file = null;
        Map<String, String> headerValue = new LinkedHashMap<>(); //放表头数据Map,带单元格格式
//        List<String> headerValue = new ArrayList<>();//放表头数据
        Map<String, String> sheetAndTableName = new HashMap<>();//放最终返回的sheet名和表名的键值对
        String insertSql = "";
        boolean sqlFlag = false;
        boolean commentSqlFlag = false;
        boolean dataSqlFlag = false;
        try {
            String[] split = multipartFile.getOriginalFilename().split("\\.");
            String eShortName = split[0];
//            String generateTableSql
            file = multipartFileToFile(multipartFile);
            Workbook wb = null;
            Sheet sheet = null;
            Row secondRow = null;
            Row row = null;
            wb = readExcel(file, multipartFile.getOriginalFilename());
            if (wb != null) {
                int numberOfSheets = wb.getNumberOfSheets();
                String sheetName = "";
                int rownum = 0;
                int column = 0;
                int tableSuccessValue = 0;//最终是否成功建表的标志符
                int tableDataSuccessValue = 0;//最终是否成功插入数据的标志符
                String errorMessage = "";//拼接所有的错误信息
                for (int n = 0; n < numberOfSheets; n++) {
                    sheet = wb.getSheetAt(n);
                    sheetName = sheet.getSheetName();
                    headerValue.clear();
                    //获取最大行数
                    rownum = sheet.getPhysicalNumberOfRows();
                    if (rownum == 0) {
                        continue;//忽略空sheet页
                    }
                    row = sheet.getRow(0);//拿第一行 表头
                    if (rownum > 1) {
                        secondRow = sheet.getRow(1);//拿第二行判断数据格式
                    }
                    //获取最大列数
                    if (row != null) {
                        column = row.getPhysicalNumberOfCells();
                    } else {
                        column = 0;
                    }
                    //获取表头
                    int cachedFormulaResultType;
                    String cellType;
                    for (int j = 0; j < column; j++) {
                        Cell cell = row.getCell(j);
                        Cell valueCell = secondRow.getCell(j);
                        if (valueCell != null) {
                            if (valueCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                if (HSSFDateUtil.isCellDateFormatted(valueCell)) {//日期型
                                    cellType = "2";
                                } else {//数值型
                                    cellType = "3";
                                }
                            } else if (valueCell.getCellType() == Cell.CELL_TYPE_FORMULA) {//公式型
                                cellType = "4";
                            } else {//文本
                                cellType = "1";
                            }
                        } else {
                            cellType = "1";
                        }
//                        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//                        }else {
//                            cellType = "1";
//                        }
                        String cellValue = cell.getRichStringCellValue().getString();
                        headerValue.put(cellValue, cellType);
                    }
//                judgeSql="DROP TABLE IF EXISTS "+"";
                    Map<String, String> sqlMap = generateTableSql(sheetName, headerValue);
                    String tableName = sqlMap.get("tableName");
                    System.out.println("-------------建表sql为：" + sqlMap.get("sql"));
                    System.out.println("-------------备注sql为：" + sqlMap.get("commentSql"));
                    sqlFlag = excelToolService.generateDataInPostgre(pVo, sqlMap.get("sql"));
                    commentSqlFlag = excelToolService.generateDataInPostgre(pVo, sqlMap.get("commentSql"));
                    //开始insert数据
                    String cellValue = "";
                    insertSql = "insert into " + tableName + " values ";
                    String tempInsertSql = "";
//                    Iterator<Row> rowItr = sheet.rowIterator();
//                    while (rowItr.hasNext()) {
//                        tempInsertSql = "";
//                        row = rowItr.next();
//                        insertSql += "(";
//                        for (int j = 0; j < column; j++) {
//                            Cell cell = row.getCell(j);
//                            if (cell != null) {
//                                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//                                    if (HSSFDateUtil.isCellDateFormatted(cell)) {//日期型
//                                        Date d = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
//                                        String format = new SimpleDateFormat("yyyy-MM-dd").format(d);
//                                        tempInsertSql += "'" + format + "',";
//                                    } else {//数值型
//                                        double numericCellValue = cell.getNumericCellValue();
//                                        tempInsertSql += "'" + numericCellValue + "',";
//                                    }
//                                } else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
//                                    double formulaCellValue = cell.getNumericCellValue();
//                                    tempInsertSql += "'" + formulaCellValue + "',";
//                                } else {//文本
//                                    cellValue = cell.getRichStringCellValue().getString();
//                                    if (cellValue.equals("")) {
//
//                                    }
//                                    if (cellValue.contains("'")) {
//                                        cellValue = cellValue.replace("'", "“");
//                                    }
//                                    tempInsertSql += "'" + cellValue + "',";
//                                }
////                                cell.setCellType(Cell.CELL_TYPE_STRING);
////                                if (cell.getRichStringCellValue() != null) {
////                                    cellValue = cell.getRichStringCellValue().getString();
////                                    if (cellValue.contains("'")) {
////                                        cellValue = cellValue.replace("'", "“");
////                                    }
////                                }
////                                insertSql += "'" + cellValue + "',";
//                            } else {
//                                tempInsertSql += "'" + cellValue + "',";
//                            }
//                        }
//                        insertSql += tempInsertSql;
//                        insertSql = insertSql.substring(0, insertSql.length() - 1);
//                        insertSql += "),";
//                    }
                    boolean blankJudgeFlag = false;
                    Cell cell = null;
                    for (int i = 1; i < rownum; i++) {
                        tempInsertSql = "";
                        row = sheet.getRow(i);
                        if (row == null) {
                            continue;
                        }
                        insertSql += "(";
                        for (int j = 0; j < column; j++) {
                            cell = row.getCell(j);
                            if (cell != null) {
                                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                    if (HSSFDateUtil.isCellDateFormatted(cell)) {//日期型
                                        Date d = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
                                        String format = new SimpleDateFormat("yyyy-MM-dd").format(d);
                                        tempInsertSql += "'" + format + "',";
                                    } else {//数值型
                                        double numericCellValue = cell.getNumericCellValue();
                                        String value = String.valueOf(numericCellValue);
                                        if(value != null && value.length() > 2){
                                            String checkValue = value.substring(value.length() - 2,value.length());
                                            if(checkValue != null && checkValue.equals(".0")){
                                                value = value.substring(0,value.length()-2);
                                            }
                                            tempInsertSql += "'" + value + "',";
                                        }else{
                                            tempInsertSql += "'" + numericCellValue + "',";
                                        }
//                                        tempInsertSql += "'" + numericCellValue + "',";
                                    }
                                } else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                    double formulaCellValue = cell.getNumericCellValue();
                                    String value = String.valueOf(formulaCellValue);
                                    if(value != null && value.length() > 2){
                                        String checkValue = value.substring(value.length() - 2,value.length());
                                        if(checkValue != null && checkValue.equals(".0")){
                                            value = value.substring(0,value.length()-2);
                                        }
                                        tempInsertSql += "'" + value + "',";
                                    }else{
                                        tempInsertSql += "'" + formulaCellValue + "',";
                                    }
//                                    tempInsertSql += "'" + formulaCellValue + "',";
                                } else {//文本
                                    cellValue = cell.getRichStringCellValue().getString();
//                                    if (cellValue.equals("")) {
//                                        blankJudgeFlag = true;
//                                        cellValue = "-";
//                                        break;
//                                    }
                                    if (cellValue.contains("'")) {
                                        cellValue = cellValue.replace("'", "“");
                                    }

                                    if(cellValue != null && cellValue.length() > 2){
                                        String checkValue = cellValue.substring(cellValue.length() - 2,cellValue.length());
                                        if(checkValue != null && checkValue.equals(".0")){
                                            cellValue = cellValue.substring(0,cellValue.length()-2);
                                        }
                                    }

                                    tempInsertSql += "'" + cellValue + "',";
                                }
//                                cell.setCellType(Cell.CELL_TYPE_STRING);
//                                if (cell.getRichStringCellValue() != null) {
//                                    cellValue = cell.getRichStringCellValue().getString();
//                                    if (cellValue.contains("'")) {
//                                        cellValue = cellValue.replace("'", "“");
//                                    }
//                                }
//                                insertSql += "'" + cellValue + "',";
                            } else {
                                tempInsertSql += "null,";
//                                tempInsertSql += "'" + cellValue + "',";
                            }
                        }
//                        if (blankJudgeFlag) {
//                            errorMessage += "第" + String.valueOf(n + 1) + "个sheet中第" + String.valueOf(i + 1) + "行数据不符合规范！";
//                            break;
//                        }
                        insertSql += tempInsertSql;
                        insertSql = insertSql.substring(0, insertSql.length() - 1);
                        insertSql += "),";
                    }
                    if (blankJudgeFlag) {
                        break;
                    }
                    insertSql = insertSql.substring(0, insertSql.length() - 1);
                    System.out.println("----------插入数据sql为：" + insertSql);
                    dataSqlFlag = excelToolService.generateDataInPostgre(pVo, insertSql);
                    if (sqlFlag && commentSqlFlag) {
                        tableSuccessValue += 1;
                        sheetAndTableName.put(sheetName, tableName);
                    } else {
                        errorMessage += "第" + String.valueOf(tableSuccessValue + 1) + "个sheet建表失败\n";
                        break;
                    }
                    if (dataSqlFlag) {
                        tableDataSuccessValue += 1;
                    } else {
                        errorMessage += "第" + String.valueOf(tableDataSuccessValue + 1) + "个sheet建表之后插入数据失败\n";
                        break;
                    }
                }
                if (tableSuccessValue == numberOfSheets) {
                    System.out.println("-----------执行建表sql成功！！------------");
                } else {
                    System.out.println("---------" + errorMessage + "----------");
                }
                if (tableDataSuccessValue == numberOfSheets) {
                    System.out.println("-----------执行插入数据sql成功！！------------");
                } else {
                    System.out.println("---------" + errorMessage + "----------");
                }
                if (!errorMessage.equals("")) {
                    sheetAndTableName.put("errorValue", errorMessage);
                }
            }
            //处理系统生成的临时文件
            File f = new File(file.toURI());
            if (f.delete()) {
                System.out.println("临时文件删除成功");
            } else {
                System.out.println("临时文件删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sheetAndTableName.put("exceptionValue", e.getMessage());
        }
        return sheetAndTableName;
    }

    /**
     * excel导入的解析方法（只插入数据不建表）
     * @param multipartFile
     * @param id
     * @return
     */
    public Map<String, String> analysisExcelUpdate(MultipartFile multipartFile, String id) {
        //配置我们的postgreSql数据库链接信息
        PostgreConfigVo pVo = new PostgreConfigVo();
        pVo.setUrl("jdbc:postgresql://" + datasourceIpConfig + ":" + datasourcePort + "/" + schemaDesc)
                .setUser(datasourceUserName)
                .setPwd(datasourcePasswd);
        //开始处理MultipartFile
        File file = null;
        Map<String, String> sheetAndTableName = new HashMap<>();//放最终返回的执行结果状态
        String insertSql = "";
        boolean dataSqlFlag = false;
        try {
            file = multipartFileToFile(multipartFile);
            Workbook wb = null;
            Sheet sheet = null;
            Row secondRow = null;
            Row row = null;
            wb = readExcel(file, multipartFile.getOriginalFilename());
            if (wb != null) {
                int numberOfSheets = wb.getNumberOfSheets();
                String sheetName = "";
                int rownum = 0;
                int column = 0;
                int tableDataSuccessValue = 0;//最终是否成功插入数据的标志符
                String errorMessage = "";//拼接所有的错误信息
                for (int n = 0; n < numberOfSheets; n++) {
                    sheet = wb.getSheetAt(n);
                    rownum = sheet.getPhysicalNumberOfRows();
                    if (rownum == 0) {
                        continue;//忽略空sheet页
                    }
                    sheetName = sheet.getSheetName();
                    String tableName=dataExcelService.findByIdAndSheetName(id, sheetName).getSheetTableName();
                    rownum = sheet.getPhysicalNumberOfRows();
                    if (rownum == 0) {
                        continue;//忽略空sheet页
                    }
                    row = sheet.getRow(0);//拿第一行 表头
                    //获取最大列数
                    if (row != null) {
                        column = row.getPhysicalNumberOfCells();
                    } else {
                        column = 0;
                    }
                    String cellValue = "";
                    insertSql = "insert into " + tableName + " values ";
                    String tempInsertSql = "";
                    boolean blankJudgeFlag = false;
                    for (int i = 1; i < rownum; i++) {
                        tempInsertSql = "";
                        row = sheet.getRow(i);
                        if (row == null) {
                            continue;
                        }
                        insertSql += "(";
                        for (int j = 0; j < column; j++) {
                            Cell cell = row.getCell(j);
                            if (cell != null) {
                                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                    if (HSSFDateUtil.isCellDateFormatted(cell)) {//日期型
                                        Date d = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
                                        String format = new SimpleDateFormat("yyyy-MM-dd").format(d);
                                        tempInsertSql += "'" + format + "',";
                                    } else {//数值型
                                        double numericCellValue = cell.getNumericCellValue();
                                        //如果值内容后两位为.0，截取掉
                                        String value = String.valueOf(numericCellValue);
                                        if(value != null && value.length() > 2){
                                            String checkValue = value.substring(value.length() - 2,value.length());
                                            if(checkValue != null && checkValue.equals(".0")){
                                                value = value.substring(0,value.length()-2);
                                            }
                                            tempInsertSql += "'" + value + "',";
                                        }else{
                                            tempInsertSql += "'" + numericCellValue + "',";
                                        }
//                                        tempInsertSql += "'" + numericCellValue + "',";
                                    }
                                } else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                    double formulaCellValue = cell.getNumericCellValue();
                                    //如果值内容后两位为.0，截取掉
                                    String value = String.valueOf(formulaCellValue);
                                    if(value != null && value.length() > 2){
                                        String checkValue = value.substring(value.length() - 2,value.length());
                                        if(checkValue != null && checkValue.equals(".0")){
                                            value = value.substring(0,value.length()-2);
                                        }
                                        tempInsertSql += "'" + value + "',";
                                    }else{
                                        tempInsertSql += "'" + formulaCellValue + "',";
                                    }
//                                    tempInsertSql += "'" + formulaCellValue + "',";
                                } else {//文本
                                    cellValue = cell.getRichStringCellValue().getString();
//                                    if (cellValue.equals("")) {
//                                        blankJudgeFlag = true;
//                                        break;
//                                    }
                                    if (cellValue.contains("'")) {
                                        cellValue = cellValue.replace("'", "“");
                                    }
                                    //如果值内容后两位为.0，截取掉
                                    if(cellValue != null && cellValue.length() > 2){
                                        String checkValue = cellValue.substring(cellValue.length() - 2,cellValue.length());
                                        if(checkValue != null && checkValue.equals(".0")){
                                            cellValue = cellValue.substring(0,cellValue.length()-2);
                                        }
                                    }
                                    tempInsertSql += "'" + cellValue + "',";
                                }
                            } else {
                                tempInsertSql += "null,";
                            }
                        }
//                        if (blankJudgeFlag) {
//                            errorMessage += "第" + String.valueOf(n + 1) + "个sheet中第" + String.valueOf(i + 1) + "行数据不符合规范！";
//                            break;
//                        }
                        insertSql += tempInsertSql;
                        insertSql = insertSql.substring(0, insertSql.length() - 1);
                        insertSql += "),";
                    }
                    if (blankJudgeFlag) {
                        break;
                    }
                    insertSql = insertSql.substring(0, insertSql.length() - 1);
                    System.out.println("----------插入数据sql为：" + insertSql);
//                    dataSqlFlag = excelToolService.generateDataInPostgre(pVo, insertSql);
                    Map<String,Object> rMap = excelToolService.generateDataInPostgreMessage(pVo,insertSql);
                    dataSqlFlag = (boolean) rMap.get("check");
                    String message = (String) rMap.get("message");

                    if (dataSqlFlag) {
                        tableDataSuccessValue += 1;
                    } else {
                        errorMessage += "第" + String.valueOf(tableDataSuccessValue + 1) + "个sheet插入数据失败;sql错误信息：" + message + "\n";
                        break;
                    }
                }
                if (tableDataSuccessValue == numberOfSheets) {
                    System.out.println("-----------执行插入数据sql成功！！------------");
                } else {
                    System.out.println("---------" + errorMessage + "----------");
                }
                if (!errorMessage.equals("")) {
                    sheetAndTableName.put("errorValue", errorMessage);
                }
            }
            //处理系统生成的临时文件
            File f = new File(file.toURI());
            if (f.delete()) {
                System.out.println("临时文件删除成功");
            } else {
                System.out.println("临时文件删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sheetAndTableName.put("exceptionValue", e.getMessage());
        }
        return sheetAndTableName;
    }

    /**
     * 获取excel文件的所有sheet名
     * @param multipartFile
     * @return
     */
    public List<String> getExcelSheetNames(MultipartFile multipartFile){
        //开始处理MultipartFile
        File file = null;
        List<String> sheetNamesList = new ArrayList<>();
        try {
            file = multipartFileToFile(multipartFile);
            Workbook wb = null;
            Sheet sheet = null;
            wb = readExcel(file, multipartFile.getOriginalFilename());
            if (wb != null) {
                int numberOfSheets = wb.getNumberOfSheets();
                for (int n = 0; n < numberOfSheets; n++) {
                    sheet = wb.getSheetAt(n);
                    sheetNamesList.add(sheet.getSheetName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sheetNamesList;
    }

    /**
     * MultipartFile转为File
     *
     * @param multipartFile
     * @return
     * @throws Exception
     */
    public static File multipartFileToFile(MultipartFile multipartFile) throws Exception {
        File toFile = null;
        if (multipartFile.equals("") || multipartFile.getSize() <= 0) {
            multipartFile = null;
        } else {
            InputStream ins = null;
            ins = multipartFile.getInputStream();
            toFile = new File(multipartFile.getOriginalFilename());
            inputStreamToFile(ins, toFile);
            ins.close();
        }
        return toFile;
    }

    /**
     * 获取流文件
     *
     * @param ins
     * @param file
     */
    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取excel文件为workbook
     *
     * @param file
     * @param fileName
     * @return
     */
    public static Workbook readExcel(File file, String fileName) {
        Workbook wb = null;
        InputStream is = null;
        if (fileName == null) {
            return null;
        }
        String extString = fileName.substring(fileName.lastIndexOf("."));
        try {
            is = new FileInputStream(file);
            if (".xls".equals(extString)) {
                wb = new HSSFWorkbook(is);
            } else if (".xlsx".equals(extString)) {
                wb = new XSSFWorkbook(is);
            } else {
                wb = null;
            }
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wb;
    }

    /**
     * 生成建表执行sql（1.建表sql  2.备注sql）
     *
     * @param sheetName
     * @param headerValues
     * @return
     */
    public Map<String, String> generateTableSql(String sheetName, Map<String,String> headerValues) {
        Pattern p = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher m = p.matcher(sheetName);
        String tableName = "";
        if (m.find()) {//有中文
            tableName = excelToolService.getFullSpellPingYin(sheetName);
            String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
            Pattern pp = Pattern.compile(regEx);
            Matcher mm = pp.matcher(tableName);
            tableName=mm.replaceAll("").trim();
        } else {//无中文
            //去掉所有特殊字符
            tableName = sheetName;
            String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
            Pattern pp = Pattern.compile(regEx);
            Matcher mm = pp.matcher(tableName);
            tableName=mm.replaceAll("").trim();
        }
        //长度过长则截取
        if (tableName.length() > 40) {
            tableName = tableName.substring(0, 40);
        }
        tableName = tableName.toLowerCase();
        tableName += System.currentTimeMillis() / 1000;//加上时间戳
        tableName = tableName.replaceAll(" ", "");
        tableName = "t_" + tableName;
//        System.out.println("-----------表名为：" + tableName);
        String sql = "create table " + tableName + " ( ";
        String commentSql = "comment on table " + tableName + " is '" + sheetName + "';";
        List<String> newNameList = new ArrayList<>();
        for (Map.Entry<String, String> e : headerValues.entrySet()) {
            String temp = excelToolService.getFullSpellPingYin(e.getKey());
            String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
            Pattern pp = Pattern.compile(regEx);
            Matcher mm = pp.matcher(temp);
            temp=mm.replaceAll("").trim();
            newNameList.add(temp);
        }
//        for (int i = 0; i < headerValues.size(); i++) {
//            String temp = excelToolService.getFullSpellPingYin(headerValues.get(i));
//            String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
//            Pattern pp = Pattern.compile(regEx);
//            Matcher mm = pp.matcher(temp);
//            temp=mm.replaceAll("").trim();
//            newNameList.add(temp);
//        }
        //检查nameList中是否有重复数据，因为忽略了特殊符号
        int temp;
        for (int i = 0; i < newNameList.size(); i++) {
            temp = 0;
            for (int j = i + 1; j < newNameList.size(); j++) {
                if (newNameList.get(j).equals(newNameList.get(i))) {
                    temp += 1;
                    String newValue = newNameList.get(j) + String.valueOf(temp);
                    newNameList.set(j, newValue);
                }
            }
        }
        int link = 0;
        for (Map.Entry<String, String> e : headerValues.entrySet()) {
            String dealedStr = newNameList.get(link);
            if (e.getValue().equals("2")) {
                sql += dealedStr + " date,";
            } else if (e.getValue().equals("3")||e.getValue().equals("4")) {
                sql += dealedStr + " NUMERIC,";
            } else {
                sql += dealedStr + " varchar,";
            }
            commentSql += "comment on column " +tableName + "." + dealedStr + " is '" + e.getKey() + "';";
            link++;
        }
//        for (int j = 0; j < newNameList.size(); j++) {
//            String dealedStr = newNameList.get(j);
////            if (dealedStr.contains("(") || dealedStr.contains(")")) {
////                if (dealedStr.contains("(")) {
////                    dealedStr = dealedStr.replaceAll("\\(", "_");
////                }
////                if (dealedStr.contains(")")) {
////                    dealedStr = dealedStr.replaceAll("\\)", "_");
////                }
////            }
//            sql += dealedStr + " varchar,";
//            commentSql += "comment on column " + tableName + "." + dealedStr + " is '" + headerValues.get(j) + "';";
//        }
        sql = sql.substring(0, sql.length() - 1);
        sql += ");";
        Map<String, String> sqlMap = new HashMap();
        sqlMap.put("sql", sql);
        sqlMap.put("commentSql", commentSql);
        sqlMap.put("tableName", tableName);
        return sqlMap;
    }

//    public String checSheetNameAndFieldNames(MultipartFile multipartFile, String id) {
//        List<String> sheetNamesFromDb = new ArrayList<>();
//        List<TBDatasourceExcel> listByDataSourceId = dataExcelService.getListByDataSourceId(id);
//        for (TBDatasourceExcel e : listByDataSourceId) {
//            sheetNamesFromDb.add(e.getSheetName());
//        }
//        int size = sheetNamesFromDb.size();
//        int compareNum = 0;
//        List<String> excelSheetNames = getExcelSheetNames(multipartFile);
//        for (int i = 0; i < excelSheetNames.size(); i++) {
//            for (int j = 0; j < sheetNamesFromDb.size(); j++) {
//                if (excelSheetNames.get(i).equals(sheetNamesFromDb.get(j))) {
//                    compareNum++;
//                    break;
//                }
//                continue;
//            }
//        }
//        if (compareNum != size) {
//            return "0";//sheet不匹配
//        }
//        Pattern p = Pattern.compile("[\\u4e00-\\u9fa5]");
//        for (String sheetName : excelSheetNames) {
//            Matcher m = p.matcher(sheetName);
//            String tableName = "";
//            if (m.find()) {//有中文
//                tableName = excelToolService.getFullSpellPingYin(sheetName);
//            } else {//无中文
//                tableName = sheetName;
//            }
//
//        }
//    }

    /**
     * 测试接口
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/data/datasource/xcltest")
    public RespEntity xclTest(@RequestParam("file") MultipartFile multipartFile) {
//        String str = "部门吕业绿绩女（第一季度）";
//        String fullSpellPingYin = excelToolService.getPingYin(str);
////        String regEx ="[^a-zA-Z0-9]";
//////        String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
////        Pattern p = Pattern.compile(regEx);
////        Matcher m = p.matcher(str);
////        System.out.println(m.replaceAll("").trim());
//        String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
//        Pattern pp = Pattern.compile(regEx);
//        Matcher mm = pp.matcher(fullSpellPingYin);
//        fullSpellPingYin=mm.replaceAll("").trim();
//        System.out.println(fullSpellPingYin);
//        return new RespEntity(RespCode.SUCCESS, fullSpellPingYin);
//        return new RespEntity(RespCode.SUCCESS, multipartFile.getOriginalFilename());

        try {
            File file = multipartFileToFile(multipartFile);
            String fileName = multipartFile.getOriginalFilename();
            InputStream is = new FileInputStream(file);
            Workbook wb = new XSSFWorkbook(is);
            is.close();
            int numberOfSheets = wb.getNumberOfSheets();
            for (int n = 0; n < numberOfSheets; n++) {
                Sheet sheet = wb.getSheetAt(n);
                String sheetName = sheet.getSheetName();
                int rownum = sheet.getPhysicalNumberOfRows();
                Row row = sheet.getRow(1);
                int column = row.getPhysicalNumberOfCells();
                for (int i = 0; i < column; i++) {
                    Cell cell = row.getCell(i);
                    if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            Date d = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
                            String format = new SimpleDateFormat("yyyy-MM-dd").format(d);
                            System.out.println(format);
                        } else {
                            double numericCellValue = cell.getNumericCellValue();
                            System.out.println(numericCellValue);
                        }
                    }
                    short dataFormat = cell.getCellStyle().getDataFormat();
                    int cellType = cell.getCellType();
                    String richStringCellValue = "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RespEntity(RespCode.SUCCESS);
    }

}
