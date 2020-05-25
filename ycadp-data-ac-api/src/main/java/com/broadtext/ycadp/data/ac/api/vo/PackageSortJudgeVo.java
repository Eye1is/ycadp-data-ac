package com.broadtext.ycadp.data.ac.api.vo;

import lombok.Data;

@Data
public class PackageSortJudgeVo {
    /**
     * "0"  组内拖动；"1"  跨组拖动
     */
    String moveType;
    /**
     * true：允许拖动，组内拖动不需要提示，跨组拖动要弹出提示框，询问是否确定拖动到其他组；false：不允许拖动，提示无权限
     */
    boolean authFlag;
}
