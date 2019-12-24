package com.broadtext.ycadp.data.ac.provider.controller;

import java.util.HashMap;
import java.util.Map;

import com.broadtext.ycadp.data.ac.api.constants.DataSourceType;
import com.broadtext.ycadp.data.ac.provider.service.DataacInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.provider.service.DataacService;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试数据源连接Controller
 *
 * @author qiaoyanbo
 */
@RestController
@Slf4j
public class DataacTestConnectController {
    /**注入依赖*/
    @Autowired
    private DataacInfoService mysql;
    @Autowired
    private DataacInfoService oracle;

    /**
     * 测试数据源连接
     * @param datasourceConfig 数据源对象
     * @return RespEntity
     */
    @PostMapping("/data/connecttest")
    public RespEntity connectDatasource(@RequestBody TBDatasourceConfig datasourceConfig){
        try {
            RespEntity respEntity = null;
            boolean result = false;
            String resultMessage = "";
            Map<Boolean,String> map = new HashMap<>();
            String datasourceType = datasourceConfig.getDatasourceType();
            if(DataSourceType.MYSQL.equals(datasourceType)) {
                map = mysql.check(datasourceConfig);
            } else if (DataSourceType.ORACLE.equals(datasourceType)){
                map = oracle.check(datasourceConfig);
            }
            for(boolean key : map.keySet()) {
            	result = key;
            	resultMessage = map.get(key);
            	break;
            }
            if(result) {
            	respEntity=new RespEntity(RespCode.SUCCESS,resultMessage);
            }else {
            	respEntity=new RespEntity(RespCode.SUCCESS,resultMessage);
            }
            return respEntity;
        }catch (Exception e){
            e.printStackTrace();
            RespEntity respEntity=new RespEntity(RespCode.SUCCESS,"连接失败");
            return respEntity;
        }
    }
}
