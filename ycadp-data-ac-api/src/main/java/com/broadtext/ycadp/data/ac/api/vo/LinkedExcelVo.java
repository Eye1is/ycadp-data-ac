package com.broadtext.ycadp.data.ac.api.vo;

import lombok.*;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LinkedExcelVo {
    private String isPage;
    private String datasourceId;
    private String createdTime;
    private String createdName;
    private String fileName;
}
