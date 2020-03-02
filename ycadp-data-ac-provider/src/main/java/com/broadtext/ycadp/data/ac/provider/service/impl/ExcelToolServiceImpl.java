package com.broadtext.ycadp.data.ac.provider.service.impl;

import com.broadtext.ycadp.data.ac.api.vo.PostgreConfigVo;
import com.broadtext.ycadp.data.ac.provider.service.ExcelToolService;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * excel工具接口实现类
 */
@Service
public class ExcelToolServiceImpl implements ExcelToolService {

    @Override
    public String getFirstSpellPingYin(String chinese) {
        // 用StringBuffer（字符串缓冲）来接收处理的数据
        StringBuffer sb = new StringBuffer();
        //字符串转换为字截数组
        char[] arr = chinese.toCharArray();
        //创建转换对象
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        //转换类型（大写or小写）
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //定义中文声调的输出格式
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < arr.length; i++) {
            //判断是否是汉子字符
            if (arr[i] > 128) {
                try {
                    // 提取汉字的首字母
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat);
                    if (temp != null) {
                        sb.append(temp[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                // 如果不是汉字字符，直接拼接
                sb.append(arr[i]);
            }
        }
        return sb.toString();
    }

    @Override
    public String getFullSpellPingYin(String chinese) {
        //如果不包含汉字，那么直接去掉特殊字符返回
        Pattern p = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher m = p.matcher(chinese);
        if (!m.find()) {
            String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
            Pattern pp = Pattern.compile(regEx);
            Matcher mm = pp.matcher(chinese);
            chinese=mm.replaceAll("").trim();
            return chinese;
        }
        //包含汉字，则只取汉字，（不取中文标点符号、以及其他任何字母和符号）
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < chinese.length(); i++) {
            char t = chinese.charAt(i);
            String reg = "[\u4e00-\u9fa5]";
            String str = String.valueOf(t);
            if (str.matches(".*" + reg + ".*")) {
                b.append(str);
            }
        }
        chinese = b.toString();
        // 用StringBuffer（字符串缓冲）来接收处理的数据
        StringBuffer sb = new StringBuffer();
        //字符串转换字节数组
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        //转换类型（大写or小写）
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //定义中文声调的输出格式
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        //定义字符的输出格式
        defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_U_AND_COLON);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 128) {
                try {
                    sb.append(capitalize(PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat)[0]));
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                // 如果不是汉字字符，直接忽略
//                sb.append(arr[i]);
                continue;
            }
        }
        return sb.toString();
    }

    @Override
    public String getPingYin(String inputString) {
        //创建转换对象
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        //转换类型（大写or小写）
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //定义中文声调的输出格式
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        //定义字符的输出格式
        format.setVCharType(HanyuPinyinVCharType.WITH_U_AND_COLON);

        //转换为字节数组
        char[] input = inputString.trim().toCharArray();
        // 用StringBuffer（字符串缓冲）来接收处理的数据
        StringBuffer output = new StringBuffer();

        try {
            for (int i = 0; i < input.length; i++) {
                //判断是否是一个汉子字符
                if (java.lang.Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
                    output.append(temp[0]);
                } else {
                    // 如果不是汉字字符，直接拼接
                    output.append(java.lang.Character.toString(input[i]));
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    @Override
    public boolean generateDataInPostgre(PostgreConfigVo pVo, String sql) {
        Connection coon = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            coon = DriverManager.getConnection(pVo.getUrl(), pVo.getUser(), pVo.getPwd());
            coon.setAutoCommit(false);
            System.out.println("开启postgre数据库成功");
            stmt = coon.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            coon.commit();
            coon.close();
        } catch (ClassNotFoundException e) {
            System.out.println("装在jdbc驱动失败");
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            System.out.println("无法连接数据库");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean generateHeaderInPostgre(PostgreConfigVo pVo, List<String> headerValue) {
        Connection coon = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            coon = DriverManager.getConnection(pVo.getUrl(), pVo.getUser(), pVo.getPwd());
            coon.setAutoCommit(false);
            System.out.println("开启postgre数据库成功");
            stmt = coon.createStatement();
            String sql = "";
            stmt.executeUpdate(sql);
            stmt.close();
            coon.commit();
            coon.close();
        } catch (ClassNotFoundException e) {
            System.out.println("装在jdbc驱动失败");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("无法连接数据库");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 首字母大写
     * @param str
     * @return
     */
    public static String capitalize(String str) {
        char ch[];
        ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        String newString = new String(ch);
        return newString;
    }

}
