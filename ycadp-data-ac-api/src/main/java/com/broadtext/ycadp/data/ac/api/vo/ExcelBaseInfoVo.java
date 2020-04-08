package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExcelBaseInfoVo {
    String id;
    String datasourceName;
    String remark;
    String cloudUrl;
    String flag;
    String packageId;
    String code;
}
