/*
 * FieldDictMapVo.java
 * Created at 2019/7/8
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class FieldDictMapVo {
    private String datasourceId;
    private String sql;
    private Map<String, List<FieldDictVo>> dictMap;
}
