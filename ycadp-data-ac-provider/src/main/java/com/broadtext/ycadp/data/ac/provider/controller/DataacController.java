package com.broadtext.ycadp.data.ac.provider.controller;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.broadtext.ycadp.base.entity.ListPager;
import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.api.vo.DataSourceListVo;
import com.broadtext.ycadp.data.ac.api.vo.TBDatasourceConfigVo;
import com.broadtext.ycadp.data.ac.provider.service.DataacService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 新增数据源
     * @param datasourceConfig
     * @return
     */
    @PostMapping("/data/datasource")
    public RespEntity addDatasource(@RequestBody TBDatasourceConfigVo datasourceConfig){
        RespEntity respEntity=null;
        TBDatasourceConfig dasource=new TBDatasourceConfig();
        dasource.setDatasourceName(datasourceConfig.getDatasourceName());
        dasource.setDatasourceType(datasourceConfig.getDatasourceType());
        dasource.setConnectionIp(datasourceConfig.getConnectionIp());
        dasource.setConnectionPort(datasourceConfig.getConnectionPort());
        dasource.setDatasourceUserName(datasourceConfig.getDatasourceUserName());
        dasource.setDatasourcePasswd(datasourceConfig.getDatasourcePasswd());
        dasource.setDictSql(datasourceConfig.getDictSql());
        dasource.setRemark(datasourceConfig.getRemark());
        dasource.setSchemaDesc(datasourceConfig.getSchemaDesc());
        TBDatasourceConfig result=dataacService.addOne(dasource);
        if (result!=null){
            datasourceConfig.setId(result.getId());
            respEntity=new RespEntity(RespCode.SUCCESS,datasourceConfig);
        }else {
            respEntity=new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }


    /**
     * 删除数据源
     * @param id
     * @return
     */
    @DeleteMapping("/data/datasource/{id}")
    public RespEntity deleteDatasource(@PathVariable("id") String id){
        try {
            dataacService.removeOne(id);
            RespEntity respEntity=new RespEntity(RespCode.SUCCESS);
            return respEntity;
        }catch (Exception e){
            e.printStackTrace();
            RespEntity respEntity=new RespEntity(DataacRespCode.DATAAC_RESP_CODE,e.getMessage());
            return respEntity;
        }
    }


    /**
     * 查询数据源列表
     * @param request
     * @param pager
     * @return
     */
    @GetMapping("/data/datasource")
    public RespEntity getDatasources(HttpServletRequest request, ListPager<DataSourceListVo> pager){
        RespEntity respEntity=null;
        String ispage=request.getParameter("isPage");
        String datasourceName=request.getParameter("datasourceName");
        List<TBDatasourceConfig> datas=new ArrayList<>();
        if (datasourceName==null){//无筛选条件查询所有
            datas=dataacService.getList();
        }else if (!"".equals(datasourceName)){//有筛选条件
            datas=dataacService.getListByDatasourceName(datasourceName);
        }else {//无筛选条件查询所有
            datas=dataacService.getList();
        }
        List<DataSourceListVo> voDatas=new ArrayList<>();
        for (TBDatasourceConfig daSource : datas){
            DataSourceListVo vo=new DataSourceListVo();
            vo.setId(daSource.getId());
            vo.setDatasourceName(daSource.getDatasourceName());
            voDatas.add(vo);
        }
        if (ispage==null||"true".equals(ispage)){
            //分页
            String sort=request.getParameter("sort");
            String pageNum=request.getParameter("pageNum");
            String pageSize=request.getParameter("pageSize");
            if (pageNum==null||pageSize==null||"".equals(pageNum)||"".equals(pageSize)){
                respEntity=new RespEntity(DataacRespCode.DATAAC_RESP_CODE, "请求分页参数有误");
            }else {
                pager.setList(voDatas);
                pager.setPageNum(Integer.parseInt(pageNum));
                pager.setPageSize(Integer.parseInt(pageSize));
                pager.setTotal(voDatas.size());
                if (sort!=null||!"".equals(sort)){
                    pager.setSort(sort);
                }
                respEntity=new RespEntity(RespCode.SUCCESS,pager);
            }
        }else {
            //不分页
            pager.setList(voDatas);
            pager.setPageNum(1);
            pager.setTotal(voDatas.size());//所有行记录数
            respEntity=new RespEntity(RespCode.SUCCESS,pager);
        }
        return respEntity;
    }
    
    
    
    /**
              * 查询数据源明细信息
     * @param id
     * @return
     */
    @GetMapping("/data/datasource/{id}")
    public RespEntity getDatasource(@PathVariable("id") String id){
        try {
            TBDatasourceConfig datasource = dataacService.findById(id);
            RespEntity respEntity = null;
            if(datasource != null) {
            	respEntity = new RespEntity(RespCode.SUCCESS, datasource);
            }else {
            	respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
            }
            return respEntity;
        }catch (Exception e){
            e.printStackTrace();
            RespEntity respEntity=new RespEntity(DataacRespCode.DATAAC_RESP_CODE,e.getMessage());
            return respEntity;
        }
    }
    
    /**
              * 编辑数据源
     * @param id
     * @param datasourceConfig
     * @return
     */
    @PutMapping("/data/datasource/{id}")
    public RespEntity updateDatasource(@PathVariable("id") String id,@RequestBody TBDatasourceConfigVo datasourceConfig){
        RespEntity respEntity=null;
        TBDatasourceConfig dasource=dataacService.findById(id);
        if(dasource != null) {
        	 dasource.setDatasourceName(datasourceConfig.getDatasourceName());
             dasource.setDatasourceType(datasourceConfig.getDatasourceType());
             dasource.setConnectionIp(datasourceConfig.getConnectionIp());
             dasource.setConnectionPort(datasourceConfig.getConnectionPort());
             dasource.setDatasourceUserName(datasourceConfig.getDatasourceUserName());
             dasource.setDatasourcePasswd(datasourceConfig.getDatasourcePasswd());
             dasource.setDictSql(datasourceConfig.getDictSql());
             dasource.setRemark(datasourceConfig.getRemark());
             dasource.setSchemaDesc(datasourceConfig.getSchemaDesc());
             dataacService.updateOne(dasource);
             datasourceConfig.setId(id);
             respEntity=new RespEntity(RespCode.SUCCESS,datasourceConfig);
        }else {
        	respEntity=new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
        return respEntity;
    }


}
