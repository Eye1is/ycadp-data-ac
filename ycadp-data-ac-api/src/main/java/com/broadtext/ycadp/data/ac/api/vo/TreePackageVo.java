package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;

import java.util.List;

@Data
public class TreePackageVo {
    String id;
    String groupId;
    String packageName;
    String sortNum;
    List<DataSourceListVo> dataSourceVoList;
}
