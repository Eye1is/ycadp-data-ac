package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;

import java.util.List;

@Data
public class GroupVo {
    String id;
    String groupName;
    String sortNum;
    List<PackageVo> packageVoList;
}
