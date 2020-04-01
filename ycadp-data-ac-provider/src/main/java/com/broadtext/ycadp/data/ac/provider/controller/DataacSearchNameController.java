package com.broadtext.ycadp.data.ac.provider.controller;

import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceGroup;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourcePackage;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.provider.service.DataacGroupService;
import com.broadtext.ycadp.data.ac.provider.service.DataacPackageService;
import com.broadtext.ycadp.data.ac.provider.service.DataacService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据接入通过名称查询数据源id Controller
 *
 * @author liuguangxuan
 */
@RestController
@Slf4j
public class DataacSearchNameController {

    @Autowired
    private DataacService dataacService;
    @Autowired
    private DataacGroupService dataacGroupService;
    @Autowired
    private DataacPackageService dataacPackageService;

    @GetMapping("/data/datasource/datasourceName")
    public RespEntity findDatasourceId(){

        RespEntity respEntity = null;
        String datasourceId="";
        String packageId="";
        try{
            //查找组
            List<TBDatasourceGroup> tbDatasourceGroup=dataacGroupService.findByName("云驰平台");

            //查找包
            if(tbDatasourceGroup.size()>0) {
                List<TBDatasourcePackage> tbDatasourcePackage = dataacPackageService.findByGroupId(tbDatasourceGroup.get(0).getId());
                //查找数据源
                if (tbDatasourcePackage.size() > 0) {
                    for(TBDatasourcePackage item : tbDatasourcePackage){
                        if(item.getPackageName().equals("默认包")){
                            packageId=item.getId();
                            List<TBDatasourceConfig> tbDatasourceConfig = dataacService.getDatasourceByPackageId(item.getId());
                            if(tbDatasourceConfig.size()>0){
                                for(TBDatasourceConfig items :tbDatasourceConfig){
                                    if(items.getDatasourceName().equals("activiti")){
                                        datasourceId=items.getId();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Map<String,String> map=new HashMap<>();
            map.put("datasourceId",datasourceId);
            map.put("packageId",packageId);
            respEntity = new RespEntity<>(RespCode.SUCCESS,map);
        } catch (Exception e) {
            respEntity = new RespEntity<>(DataacRespCode.DATAAC_RESP_CODE, "查询出错");
        }
        return respEntity;
    }
}
