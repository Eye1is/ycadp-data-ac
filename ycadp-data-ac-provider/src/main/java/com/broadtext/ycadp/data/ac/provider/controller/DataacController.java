package com.broadtext.ycadp.data.ac.provider.controller;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.broadtext.ycadp.base.entity.ListPager;
import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceGroup;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourcePackage;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.api.vo.*;
import com.broadtext.ycadp.data.ac.provider.service.DataacGroupService;
import com.broadtext.ycadp.data.ac.provider.service.DataacPackageService;
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
    @Autowired
    private DataacGroupService dataacGroupService;
    @Autowired
    private DataacPackageService dataacPackageService;

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
            vo.setDatasourceType(daSource.getDatasourceType());
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

    /**
     * 新增group
     * @param groupVo
     * @return
     */
    @PostMapping("/data/datasource/group")
    public RespEntity addGroup(@RequestBody GroupVo groupVo){
        TBDatasourceGroup groupEntity=new TBDatasourceGroup();
        groupEntity.setGroupName(groupVo.getGroupName());
        int maxSortNum=0;
        List<TBDatasourceGroup> list = dataacGroupService.getList();
        for (TBDatasourceGroup group : list){
            int tempSortNum = Integer.valueOf(group.getSortNum());
            if (tempSortNum>maxSortNum){
                maxSortNum=tempSortNum;
            }
        }
        groupEntity.setSortNum(String.valueOf(++maxSortNum));
//        groupEntity.setSortNum(groupVo.getSortNum());
        TBDatasourceGroup tbDatasourceGroup = dataacGroupService.addOne(groupEntity);
        if (tbDatasourceGroup!=null){
            return new RespEntity(RespCode.SUCCESS,tbDatasourceGroup);
        }else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }

    /**
     * 编辑group
     * @param id
     * @param groupVo
     * @return
     */
    @PutMapping("/data/datasource/group/{id}")
    public RespEntity editGroup(@PathVariable("id") String id,@RequestBody GroupVo groupVo){
        TBDatasourceGroup byId = dataacGroupService.findById(id);
        byId.setGroupName(groupVo.getGroupName());
//        byId.setSortNum(groupVo.getSortNum());
        TBDatasourceGroup tbDatasourceGroup = dataacGroupService.updateOne(byId);
        if (tbDatasourceGroup!=null){
            return new RespEntity(RespCode.SUCCESS,tbDatasourceGroup);
        }else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }

    /**
     * 回显group
     * @param id
     * @return
     */
    @GetMapping("/data/datasource/group/{id}")
    public RespEntity echoGroup(@PathVariable("id") String id){
        TBDatasourceGroup byId = dataacGroupService.findById(id);
        return new RespEntity(RespCode.SUCCESS, byId);
    }

    /**
     * 删除group
     * @param id
     * @return
     */
    @DeleteMapping("/data/datasource/group/{id}")
    public RespEntity deleteGroup(@PathVariable("id") String id){
        String sortNum=dataacGroupService.getOne(id).getSortNum();
        int sortNumInt = Integer.valueOf(sortNum);
        List<TBDatasourceGroup> list = dataacGroupService.getList();
        for (TBDatasourceGroup group : list){
            int tempSortNum=Integer.valueOf(group.getSortNum());
            if (tempSortNum>sortNumInt){
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
     * @param packageAddVo
     * @return
     */
    @PostMapping("/data/datasource/package")
    public RespEntity addPackage(@RequestBody PackageAddVo packageAddVo){
        TBDatasourcePackage tbDatasourcePackage=new TBDatasourcePackage();
        tbDatasourcePackage.setGroupId(packageAddVo.getGroupId());
        tbDatasourcePackage.setPackageName(packageAddVo.getPackageName());
        int maxSortNum=0;
        List<TBDatasourcePackage> listByGroupId = dataacPackageService.getOrderedListByGroupId(packageAddVo.getGroupId());
        for (TBDatasourcePackage p : listByGroupId){
            int tempSortNum = Integer.valueOf(p.getSortNum());
            if (tempSortNum>maxSortNum){
                maxSortNum=tempSortNum;
            }
        }
        tbDatasourcePackage.setSortNum(String.valueOf(++maxSortNum));
//        tbDatasourcePackage.setSortNum(packageAddVo.getSortNum());
        TBDatasourcePackage result = dataacPackageService.addOne(tbDatasourcePackage);
        if (result!=null){
            return new RespEntity(RespCode.SUCCESS,result);
        }else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }

    }

    /**
     * 编辑package
     * @param id
     * @param packageAddVo
     * @return
     */
    @PutMapping("/data/datasource/package/{id}")
    public RespEntity editPackage(@PathVariable("id") String id,@RequestBody PackageAddVo packageAddVo){
        TBDatasourcePackage byId = dataacPackageService.findById(id);
        byId.setPackageName(packageAddVo.getPackageName());
//        byId.setSortNum(packageAddVo.getSortNum());
        TBDatasourcePackage tbDatasourcePackage = dataacPackageService.updateOne(byId);
        if (tbDatasourcePackage!=null){
            return new RespEntity(RespCode.SUCCESS, tbDatasourcePackage);
        }else {
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE);
        }
    }

    /**
     * 回显package
     * @param id
     * @return
     */
    @GetMapping("/data/datasource/package/{id}")
    public RespEntity echoPackage(@PathVariable("id") String id){
        TBDatasourcePackage byId = dataacPackageService.findById(id);
        return new RespEntity(RespCode.SUCCESS, byId);
    }

    /**
     * 删除package
     * @param id
     * @return
     */
    @DeleteMapping("/data/datasource/package/{id}")
    public RespEntity deletePackage(@PathVariable("id") String id){
        TBDatasourcePackage one = dataacPackageService.getOne(id);
        int sortNum=Integer.valueOf(one.getSortNum());
        List<TBDatasourcePackage> listByGroupId = dataacPackageService.getOrderedListByGroupId(one.getGroupId());
        for (TBDatasourcePackage p : listByGroupId){
            int tempSortNum=Integer.valueOf(p.getSortNum());
            if (tempSortNum>sortNum){
                p.setSortNum(String.valueOf(--tempSortNum));
                dataacPackageService.updateOne(p);
            }
        }
        dataacPackageService.removeOne(id);
        return new RespEntity(RespCode.SUCCESS);
    }

    /**
     * 包结构树形展示（完整树形）
     * @return
     */
    @GetMapping("/data/datasource/tree")
    public RespEntity viewTree(){
        List<GroupVo> groupVoList = new ArrayList<>();
        List<TBDatasourceGroup> groupList = dataacGroupService.getListBySortNum();//排序过的组List
        List<TBDatasourcePackage> packageList;
        for (TBDatasourceGroup g:groupList){
            GroupVo gVo=new GroupVo();
            gVo.setId(g.getId());
            gVo.setGroupName(g.getGroupName());
            gVo.setSortNum(g.getSortNum());
            List<PackageVo> packageVoList = new ArrayList<>();
            packageList=dataacPackageService.getOrderedListByGroupId(g.getId());
            for (TBDatasourcePackage p:packageList){
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

    /**
     * 移动改变顺序
     * @param sortGroupVo
     * @return
     */
    @PostMapping("/data/datasource/sort")
    public RespEntity changeSort(@RequestBody SortGroupVo sortGroupVo){
        List<GroupVo> groupVoList = sortGroupVo.getGroupVoList();
        for (GroupVo gVo : groupVoList){
            TBDatasourceGroup theGroup = dataacGroupService.getOne(gVo.getId());//该vo对应的真实group实体
            theGroup.setSortNum(gVo.getSortNum());
            dataacGroupService.updateOne(theGroup);
            List<PackageVo> packageVoList = gVo.getPackageVoList();
            for (PackageVo pVo : packageVoList){
                TBDatasourcePackage thePackage = dataacPackageService.getOne(pVo.getId());
                thePackage.setGroupId(pVo.getGroupId());
                thePackage.setSortNum(pVo.getSortNum());
                dataacPackageService.updateOne(thePackage);
            }
        }
        return new RespEntity(RespCode.SUCCESS);
    }

}
