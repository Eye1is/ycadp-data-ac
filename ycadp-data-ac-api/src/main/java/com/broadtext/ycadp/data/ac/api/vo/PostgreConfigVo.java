package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PostgreConfigVo {
    String url;
    String user;
    String pwd;
}
