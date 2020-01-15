package com.broadtext.ycadp.data.ac.provider.service;

import com.broadtext.ycadp.data.ac.api.vo.PostgreConfigVo;

import java.util.List;

/**
 * 解析excel相关的方法服务
 */
public interface ExcelToolService {
    /**
     * 获取汉字串拼音首字母，英文字符不变
     * @param chinese
     * @return
     */
    String getFirstSpellPingYin(String chinese);

    /**
     * 获取汉字串拼音（首字母大写），英文字符不变
     * @param chinese
     * @return
     */
    String getFullSpellPingYin(String chinese);

    /**
     * 将字符串中的中文转化为拼音(不区分大小写),其他字符不变
     * @param inputString
     * @return
     */
    String getPingYin(String inputString);

    /**
     * 在postgre中执行sql
     * @param pVo
     * @param sql
     * @return
     */
    boolean generateDataInPostgre(PostgreConfigVo pVo, String sql);

    /**
     * 在postgre中生成表头
     * @param pVo
     * @param headerValue
     * @return
     */
    boolean generateHeaderInPostgre(PostgreConfigVo pVo, List<String> headerValue);
}
