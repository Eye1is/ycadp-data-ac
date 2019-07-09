/*
 * Data.java
 * Created at 2019/7/8
 * Created by ouhaoliang
 * Copyright (C) 2019 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;

@Data
public class DatasourceDictVo {
    private String datasourceId;
    private String dictSql;
    private String dictKey;
}
