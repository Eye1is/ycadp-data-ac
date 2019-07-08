package com.broadtext.ycadp.data.ac.provider.controller;

import com.alibaba.fastjson.JSON;
import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.provider.service.DataacInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库表查询Controller
 *
 * @author liuguangxuan
 */
@RestController
@Slf4j
public class DataacSearchController {
    /**服务层依赖注入*/
    @Autowired
    private DataacInfoService dataacInfoService;

    /**
     * 查找数据库表列表
     * @param id 对象id
     * @param tableName 表名
     * @return  返回接口数据
     */
    @GetMapping("/data/datatable/{id}")
    public RespEntity searchTables(@PathVariable(value="id") String id,String tableName) {
        TBDatasourceConfig datasource=dataacInfoService.findById(id);
        List<String> list=new ArrayList<String>();
        List<String> listContains=new ArrayList<String>();
        Map map =new HashMap();
        try {
            if (tableName==null){//无筛选条件查询所有
                list= dataacInfoService.getAllTables(datasource);
                map.put("list",list);
            }else if (!"".equals(tableName)){//有筛选条件
                list= dataacInfoService.getAllTables(datasource);
                if(list.size()>0){
                    for (String str : list) {
                        if(str.contains(tableName)){
                            listContains.add(str);
                        }
                    }
                }
                map.put("list",listContains);
            }else {//无筛选条件查询所有
                list= dataacInfoService.getAllTables(datasource);
                map.put("list",list);
            }
            return new RespEntity(RespCode.SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }
    /**
     * 查询数据库表数据
     * @param id 对象id
     * @param request 请求request
     * @return 返回接口数据
     */
    @GetMapping("data/datatables/{id}")
    public RespEntity searchDataTable(HttpServletRequest request, @PathVariable(value="id") String id) {
        TBDatasourceConfig datasource=dataacInfoService.findById(id);
        String ispage=request.getParameter("isPage");
        String tableName=request.getParameter("tableName");
//        List<Map<String, Object>> list=new ArrayList<Map<String, Object>>();
        String sql="";
        int count=0;
        Map map =new HashMap();
        if (ispage==null||"true".equals(ispage)){
            //分页
            String pageNum=request.getParameter("pageNum");
            String pageSize=request.getParameter("pageSize");
            sql="select * from "+tableName+" limit " +Integer.parseInt(pageSize)*(Integer.parseInt(pageNum)-1)+","+Integer.parseInt(pageSize);
        }else {
            //不分页
            sql="select * from "+tableName;
        }
        try {
            List allData = dataacInfoService.getAllData(datasource, sql);
            String sqlTotal="select * from "+tableName;
            String str = JSON.toJSONString(allData);
            count=dataacInfoService.getDataCount(datasource,sqlTotal);
            map.put("total",count);
            map.put("list",str);
            return new RespEntity(RespCode.SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }
}
