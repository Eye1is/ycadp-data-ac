package com.broadtext.ycadp.data.ac.api.vo;

import com.broadtext.ycadp.data.ac.api.entity.TBDatasourceConfig;
import lombok.Data;

import java.util.List;

@Data
public class PackageVo {
    String id;
    String groupId;
    String packageName;
    String sortNum;
}
