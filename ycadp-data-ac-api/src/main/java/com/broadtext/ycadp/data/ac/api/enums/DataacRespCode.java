package com.broadtext.ycadp.data.ac.api.enums;

import com.broadtext.ycadp.base.enums.RespCodeEnumI;

/**
 * 数据接入返回信息枚举类
 *
 * @author xuchenglong
 */
public enum DataacRespCode implements RespCodeEnumI{
    /**
     * 操作失败
     */
    DATAAC_RESP_CODE("212000","操作失败");

    /**
     * 错误代码
     */
    private String code;
    /**
     * 错误信息
     */
    private String msg;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    /**
     * 构造方法
     * @param code
     * @param msg
     */
    DataacRespCode(String code,String msg){
        this.code = code;
        this.msg = msg;
    }
}
