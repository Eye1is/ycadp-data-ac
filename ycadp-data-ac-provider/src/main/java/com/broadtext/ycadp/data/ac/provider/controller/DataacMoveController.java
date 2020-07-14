package com.broadtext.ycadp.data.ac.provider.controller;

import com.broadtext.ycadp.base.enums.RespCode;
import com.broadtext.ycadp.base.enums.RespEntity;
import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import com.broadtext.ycadp.data.ac.api.enums.DataacRespCode;
import com.broadtext.ycadp.data.ac.provider.service.DataacService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@SuppressWarnings("all")
public class DataacMoveController {
    @Autowired
    private DataacService dataacService;

    @GetMapping("/dataac/moveContent/{packageId}/{id}")
    public RespEntity moveContent(@PathVariable(value = "packageId") String packageId, @PathVariable(value = "id") String id) {
        RespEntity respEntity = null;
        try {
            TBDatasourceConfig datasourceConfig = dataacService.findById(id);
            if (datasourceConfig!=null){
                datasourceConfig.setPackageId(packageId);
                TBDatasourceConfig updateOne = dataacService.updateOne(datasourceConfig);
                if (packageId.equals(updateOne.getPackageId())) {
                    respEntity = new RespEntity(RespCode.SUCCESS,"移动成功");
                } else {
                    respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE,"移动失败");
                }
            } else {
                respEntity = new RespEntity(DataacRespCode.DATAAC_RESP_CODE,"id找不到实体");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new RespEntity(DataacRespCode.DATAAC_RESP_CODE,"系统异常");
        }
        return respEntity;
    }

}
