package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;

import java.util.List;

@Data
public class TreeGroupVo {
    String id;
    String groupName;
    String sortNum;
    List<TreePackageVo> packageVoList;
}
