package com.broadtext.ycadp.data.ac.provider.controller;

import com.broadtext.ycadp.base.entity.ListPager;
import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.constants.DataSourceType;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceExcel;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceGroup;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourcePackage;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.api.vo.*;
import com.broadtext.ycadp.data.ac.provider.service.*;
import com.broadtext.ycadp.data.ac.provider.utils.ArrayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * 新增数据源
     *
     * @param datasourceConfig
     * @return
     */
    @PostMapping("/data/datasource")
    public RespEntity addDatasource(@RequestBody TBDatasourceConfigVo datasourceConfig) {
        RespEntity respEntity = null;
        TBDatasourceConfig dasource = new TBDatasourceConfig();
        dasource.setDatasourceName(datasourceConfig.getDatasourceName());
        dasource.setDatasourceType(datasourceConfig.getDatasourceType());
        dasource.setConnectionIp(datasourceConfig.getConnectionIp());
        dasource.setConnectionPort(datasourceConfig.getConnectionPort());
        dasource.setDatasourceUserName(datasourceConfig.getDatasourceUserName());
        dasource.setDatasourcePasswd(datasourceConfig.getDatasourcePasswd());
        dasource.setDictSql(datasourceConfig.getDictSql());
        dasource.setRemark(datasourceConfig.getRemark());
        dasource.setSchemaDesc(datasourceConfig.getSchemaDesc());
        dasource.setPackageId(datasourceConfig.getPackageId());
        TBDatasourceConfig result = dataacService.addOne(dasource);
        if (result != null) {
            datasourceConfig.setId(result.getId());
            respEntity = new RespEntity(RespCode.SUCCESS, datasourceConfig);
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
        String excelName = multipartFile.getOriginalFilename();
        Map<String, String> analysisMap = analysisExcel(multipartFile);
        if (analysisMap.containsKey("errorValue")) {
            System.out.println("sql执行出现错误！！");
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "sql执行出现错误！！");
        } else if (analysisMap.containsKey("exceptionValue")) {
            System.out.println("解析excel文件过程出现异常！！");
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "解析excel文件过程出现异常！！");
        }
        TBDatasourceConfig datasourceConfig = new TBDatasourceConfig();
        datasourceConfig.setDatasourceName(infoVo.getDatasourceName())
                .setRemark(infoVo.getRemark())
                .setCloudUrl(infoVo.getCloudUrl())
                .setPackageId(infoVo.getPackageId())
                .setDatasourceType(DataSourceType.EXCEL)
                .setSchemaDesc(excelName);
        TBDatasourceConfig dataConfigResult = dataacService.addOne(datasourceConfig);
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
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "添加数据源实体失败！！");
        }
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
                pVo.setUrl("jdbc:postgresql://192.168.16.171:5432/postgres")
                        .setUser("postgres")
                        .setPwd("postgres");
                boolean b = excelToolService.generateDataInPostgre(pVo, dropTableSql);
                //再删除原有的映射关系和excel数据
                dataExcelService.deleteByDatasourceId(id);
            }
            dataacService.removeOne(id);
            RespEntity respEntity = new RespEntity(RespCode.SUCCESS);
            return respEntity;
        } catch (Exception e) {
            e.printStackTrace();
            RespEntity respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE, e.getMessage());
            return respEntity;
        }
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
                    list.add(dataSourceListVo);
                }
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
    public RespEntity updateDatasource(@PathVariable("id") String id, @RequestBody TBDatasourceConfigVo datasourceConfig) {
        RespEntity respEntity = null;
        TBDatasourceConfig dasource = dataacService.findById(id);
        if (dasource != null) {
            dasource.setDatasourceName(datasourceConfig.getDatasourceName());
            dasource.setDatasourceType(datasourceConfig.getDatasourceType());
            dasource.setConnectionIp(datasourceConfig.getConnectionIp());
            dasource.setConnectionPort(datasourceConfig.getConnectionPort());
            dasource.setDatasourceUserName(datasourceConfig.getDatasourceUserName());
            dasource.setDatasourcePasswd(datasourceConfig.getDatasourcePasswd());
            dasource.setDictSql(datasourceConfig.getDictSql());
            dasource.setRemark(datasourceConfig.getRemark());
            dasource.setSchemaDesc(datasourceConfig.getSchemaDesc());
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
                pVo.setUrl("jdbc:postgresql://192.168.16.171:5432/postgres")
                        .setUser("postgres")
                        .setPwd("postgres");
                boolean b = excelToolService.generateDataInPostgre(pVo, dropTableSql);
                //再删除原有的映射关系和excel数据
                dataExcelService.deleteByDatasourceId(id);
                //然后添加各项数据
                String excelName = multipartFile.getOriginalFilename();
                Map<String, String> analysisMap = analysisExcel(multipartFile);
                if (analysisMap.containsKey("errorValue")) {
                    System.out.println("sql执行出现错误！！");
                    return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "sql执行出现错误！！");
                } else if (analysisMap.containsKey("exceptionValue")) {
                    System.out.println("解析excel文件过程出现异常！！");
                    return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "解析excel文件过程出现异常！！");
                }
                dasource.setDatasourceName(infoVo.getDatasourceName())
                        .setRemark(infoVo.getRemark())
                        .setPackageId(infoVo.getPackageId())
                        .setCloudUrl(infoVo.getCloudUrl())
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
            //先删除pg数据库中的excel表数据
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
            pVo.setUrl("jdbc:postgresql://192.168.16.171:5432/postgres")
                    .setUser("postgres")
                    .setPwd("postgres");
            boolean b = excelToolService.generateDataInPostgre(pVo, dropTableSql);
            //再删除原有的映射关系和excel数据
            dataExcelService.deleteByDatasourceId(id);
            //然后添加各项数据
            String excelName = multipartFile.getOriginalFilename();
            Map<String, String> analysisMap = analysisExcel(multipartFile);
            if (analysisMap.containsKey("errorValue")) {
                System.out.println("sql执行出现错误！！");
                return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "sql执行出现错误！！");
            } else if (analysisMap.containsKey("exceptionValue")) {
                System.out.println("解析excel文件过程出现异常！！");
                return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "解析excel文件过程出现异常！！");
            }
            for (Map.Entry<String, String> entry : analysisMap.entrySet()) {
                TBDatasourceExcel excelEntity = new TBDatasourceExcel();
                excelEntity.setDatasourceId(id)
                        .setSheetName(entry.getKey())
                        .setSheetTableName(entry.getValue());
                TBDatasourceExcel excelMappingResult = dataExcelService.addOne(excelEntity);
                if (excelMappingResult.getId() == null) {
                    return new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "添加excel数据源映射关系实体失败！！");
                }
            }
        }
        return new RespEntity(RespCode.SUCCESS);
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
        List<GroupVo> groupVoList = new ArrayList<>();
        List<TBDatasourceGroup> groupList = dataacGroupService.getListBySortNum();//排序过的组List
        List<TBDatasourcePackage> packageList;
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
        if (!ArrayUtil.isEmpty(groupVoList)) {
            return new RespEntity(RespCode.SUCCESS, groupVoList);
        } else {
            return new RespEntity(RespCode.SUCCESS, new ArrayList<>());
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
     * 解析Excel文件
     * 1.将其真实数据放入postgres数据库中，每个sheet对应一张表
     * 2.将excel名与postgres库中表的映射关系放入ac库中
     * ----返回各个sheet的名称list，用在在ac库的映射表中
     *
     * @param multipartFile
     * @return
     */
    public Map<String, String> analysisExcel(MultipartFile multipartFile) {
        //配置我们的postgreSql数据库链接信息
        PostgreConfigVo pVo = new PostgreConfigVo();
        pVo.setUrl("jdbc:postgresql://192.168.16.171:5432/postgres")
                .setUser("postgres")
                .setPwd("postgres");
        //开始处理MultipartFile
        File file = null;
        List<String> headerValue = new ArrayList<>();//放表头数据
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
                    row = sheet.getRow(0);//拿第一行 表头
                    //获取最大列数
                    if (row != null) {
                        column = row.getPhysicalNumberOfCells();
                    } else {
                        column = 0;
                    }
                    //获取表头
                    for (int j = 0; j < column; j++) {
                        Cell cell = row.getCell(j);
                        String cellValue = cell.getRichStringCellValue().getString();
                        headerValue.add(cellValue);
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
                    for (int i = 1; i < rownum; i++) {
                        row = sheet.getRow(i);
                        insertSql += "(";
                        for (int j = 0; j < column; j++) {
                            Cell cell = row.getCell(j);
                            if (cell != null) {
                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                if (cell.getRichStringCellValue() != null) {
                                    cellValue = cell.getRichStringCellValue().getString();
                                }
                            }
                            insertSql += "'" + cellValue + "',";
                        }
                        insertSql = insertSql.substring(0, insertSql.length() - 1);
                        insertSql += "),";
                    }
                    if (rownum != 0) {
                        insertSql = insertSql.substring(0, insertSql.length() - 1);
                        System.out.println("----------插入数据sql为：" + insertSql);
                        dataSqlFlag = excelToolService.generateDataInPostgre(pVo, insertSql);
                    } else {
                        dataSqlFlag = true;
                    }
                    if (sqlFlag && commentSqlFlag) {
                        tableSuccessValue += 1;
                        sheetAndTableName.put(sheetName, tableName);
                    } else {
                        errorMessage += "第" + String.valueOf(tableSuccessValue + 1) + "个sheet建表失败\n";
                    }
                    if (dataSqlFlag) {
                        tableDataSuccessValue += 1;
                    } else {
                        errorMessage += "第" + String.valueOf(tableDataSuccessValue + 1) + "个sheet建表之后插入数据失败\n";
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
    public Map<String, String> generateTableSql(String sheetName, List<String> headerValues) {
        Pattern p = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher m = p.matcher(sheetName);
        String tableName = "";
        if (m.find()) {//有中文
            tableName = excelToolService.getFullSpellPingYin(sheetName);
        } else {//无中文
            tableName = sheetName;
        }
        tableName = tableName.toLowerCase();
        tableName += System.currentTimeMillis() / 1000;//加上时间戳
        tableName = tableName.replaceAll(" ", "");
//        System.out.println("-----------表名为：" + tableName);
        String sql = "create table " + tableName + " ( ";
        String commentSql = "comment on table " + tableName + " is '" + sheetName + "';";
        List<String> newNameList = new ArrayList<>();
        for (int i = 0; i < headerValues.size(); i++) {
            newNameList.add(excelToolService.getFullSpellPingYin(headerValues.get(i)));
        }
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
        for (int j = 0; j < newNameList.size(); j++) {
            String dealedStr = newNameList.get(j);
//            if (dealedStr.contains("(") || dealedStr.contains(")")) {
//                if (dealedStr.contains("(")) {
//                    dealedStr = dealedStr.replaceAll("\\(", "_");
//                }
//                if (dealedStr.contains(")")) {
//                    dealedStr = dealedStr.replaceAll("\\)", "_");
//                }
//            }
            sql += dealedStr + " varchar,";
            commentSql += "comment on column " + tableName + "." + dealedStr + " is '" + headerValues.get(j) + "';";
        }
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
        String str = "jhghjfv你好hjk,";

        String testStr = "gongzuoliang(shi)";
        if (testStr.contains("(") || testStr.contains(")")) {
            if (testStr.contains("(")) {
                testStr = testStr.replaceAll("\\(", "_");
            }
            if (testStr.contains(")")) {
                testStr = testStr.replaceAll("\\)", "_");
            }
        }
        str = str.substring(0, str.length() - 1);
        return new RespEntity(RespCode.SUCCESS, testStr);
//        return new RespEntity(RespCode.SUCCESS, multipartFile.getOriginalFilename());
    }

}
