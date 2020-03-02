package com.broadtext.ycadp.data.ac.provider.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * 网盘工具包
 *
 * @author wuhui
 * @version 1.0
 */
@Slf4j
@SuppressWarnings({"rawtypes", "unchecked"})
public class GeneralUtils {


    /**
     * 读取缓冲字节数
     */
    public static final int BYTE = 2048;

    /**
     * 1000以内随机数
     */
    public static final int RANDOM = 1000;

    /**
     * 编码格式默认值
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * 获取文件字节
     *
     * @param f 文件
     * @return 字节数组
     */
    public static byte[] getBytes(File f) {
        BufferedInputStream bis = null;
        ByteArrayOutputStream bas = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(f), 2 * BYTE);
            bas = new ByteArrayOutputStream();
            byte[] bytes = new byte[BYTE];
            int len;
            while ((len = bis.read(bytes, 0, bytes.length)) != -1) {
                bas.write(bytes, 0, len);
            }
            bytes = bas.toByteArray();
            return bytes;
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bas != null) {
                    bas.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * 上传文件到本地
     *
     * @param mf          文件流接收信息
     * @param fastdfsPath 工作路径
     * @return 文件
     */
    public static File upLocalFile(MultipartFile mf, String fastdfsPath) {
        try {
            File f = new File(Objects.requireNonNull(createdFileName(mf.getOriginalFilename(), fastdfsPath)));
            //nio处理流信息
            mf.transferTo(f);
//            nioUpHandle(mf, f);
            return f;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * nio读写文件
     *
     * @param mf       文件流信息
     * @param fileCopy 拷贝文件
     */
    public static void nioUpHandle(MultipartFile mf, File fileCopy) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inchannel = null;
        FileChannel outchannel = null;
        try {
            fis = (FileInputStream) mf.getInputStream();
            fos = new FileOutputStream(fileCopy);
            inchannel = fis.getChannel();
            outchannel = fos.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(BYTE / 2);
            while (inchannel.read(buf) != -1) {
                buf.flip();
                outchannel.write(buf);
                buf.clear();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (outchannel != null) {
                    outchannel.close();
                }
                if (inchannel != null) {
                    inchannel.close();
                }
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

        }
    }


    /**
     * 递归删除文件
     *
     * @param dir       目录文件
     * @param firstPath 文件首目录
     * @return RespEntity
     */
    public static String deleteDir(File dir, File firstPath) {
        dir.delete();
        String path = dir.getParent();
        File deleteDir = new File(path);
        if (!deleteDir.getAbsolutePath().equals(firstPath.getAbsolutePath()) && deleteDir.exists() && deleteDir.isDirectory()) {
            deleteDir(deleteDir, firstPath);
        }
        return "删除成功";
    }

    /**
     * 创建文件到本地
     *
     * @param fileName    文件名
     * @param content     文件内容
     * @param fastdfsPath 工作目录
     * @return 文件信息
     */
    public static File createLocalFile(String fileName, String content, String fastdfsPath) {
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            //如果文件内容为空或"",创建空文件到本地
            if (content == null || "".equals(content)) {
                File file = new File(createdFileName(fileName, fastdfsPath));
                if (!file.exists()) {
                    file.createNewFile();
                }
                return file;
            }
            String localFileName = createdFileName(fileName, fastdfsPath);
            File writeFile = new File(localFileName);
            if (!writeFile.exists()) {
                writeFile.createNewFile();
            }
            bis = new BufferedInputStream(new ByteArrayInputStream(content.getBytes()));
            bos = new BufferedOutputStream(new FileOutputStream(writeFile));
            int length;
            byte[] b = new byte[BYTE / 2];
            while ((length = bis.read(b, 0, b.length)) != -1) {
                bos.write(b, 0, length);
            }
            return writeFile;
        } catch (Exception i) {
            log.error(i.getMessage());
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 生城随机文件目录
     *
     * @param fileName 文件名
     * @param firstDir 文件根目录
     * @return 文件绝对路径
     */
    public static String createdFileName(String fileName, String firstDir) {
        try {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            int randomNum = new Random().nextInt(RANDOM);
            long currentTime = System.currentTimeMillis();
            String loadPath = firstDir + File.separator + currentTime + File.separator + uuid + randomNum;
            File localPathFile = new File(loadPath);
            if (!localPathFile.exists()) {
                localPathFile.mkdirs();
            }
            String localFileName = localPathFile + File.separator + fileName;
            File localFile = new File(localFileName);
            if (!localFile.exists()) {
                localFile.createNewFile();
            }
            return localPathFile + File.separator + fileName;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 通过字节判断编码格式
     *
     * @param bytes 字节数组
     * @return 编码格式
     */
    public static String getEncoding(byte[] bytes) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        return encoding;
    }

    /**
     * 字段重名处理
     *
     * @param <T>          返回对象类型
     * @param t            判断重名处理对象
     * @param ts           可能重名的所有对象集合
     * @param compareField 效验重名的字段
     * @return 处理后对象
     */
    public static <T> T fileValidation(T t, List<T> ts, String compareField) {
        //如果数据库不存在同名文件,不做重名处理
        if (ts == null || ts.isEmpty()) {
            return t;
        }
        Map map = getFieldValueByName(compareField, t);
        String oldName = "";
        if (Objects.nonNull(map) && !map.isEmpty()) {
            oldName = (String) map.get(compareField);
        }
        //获取文件前缀
        String namePrefix = FilenameUtils.getBaseName(oldName);
        //获取文件后缀
        String fileType = FilenameUtils.getExtension(oldName);
        int i = 0;
        int sum = 1;
        //保留重名文件
        Iterator<T> iteratorMap = ts.iterator();
        while (iteratorMap.hasNext()) {
            T tt = iteratorMap.next();
            Map<String, Object> fieldValueByNameMap = getFieldValueByName(compareField, tt);
            String fileName = "";
            if (fieldValueByNameMap != null && !fieldValueByNameMap.isEmpty()) {
                fileName = (String) fieldValueByNameMap.get(compareField);
            }
            //排除原文件
            if (oldName.equals(fileName)) {
                iteratorMap.remove();
                i = 1;
                continue;
            }
            if ((oldName.contains(".") && !fileName.contains(".")) ||
                    (!oldName.contains(".") && fileName.contains(".")) ||
                    (oldName.contains(".") && !fileName.substring(fileName.lastIndexOf('.') + 1).equals(fileType)) ||
                    (!"(".equals(fileName.substring(namePrefix.length(), namePrefix.length() + 1)))) {
                iteratorMap.remove();
            }
        }
        //若存在同名文件且包含多个重名文件,比较并获取最大值
        if (i == 1 && !ts.isEmpty()) {
            sum = fieldRepeat(ts, compareField);
        }
        //若库中有同名文件设置名称
        if (i == 1) {
            map.put(compareField, namePrefix + "(" + sum + ")");
            if (oldName.contains(".")) {
                map.put(compareField, namePrefix + "(" + sum + ")." + fileType);
            }
            //设置对象属性值
            setFieldsValue(t, map);
        }
        return t;
    }

    /**
     * 字段重名处理
     *
     * @param <T>          返回对象类型
     * @param t            判断重名处理对象
     * @param ts           可能重名的所有对象集合
     * @param compareField 效验重名的字段
     * @return 处理后对象
     */
    public static <T> T fileNameValidation(T t, List<T> ts, String compareField) {
        //如果数据库不存在同名文件,不做重名处理
        if (ts == null || ts.isEmpty()) {
            return t;
        }
        Map<String, Object> map = getFieldValueByName(compareField, t);
        String oldName = "";
        if (map != null && !map.isEmpty()) {
            oldName = (String) map.get(compareField);
        }
        //获取文件前缀
        String namePrefix = FilenameUtils.getBaseName(oldName);
        //获取文件后缀
        String fileType = FilenameUtils.getExtension(oldName);
        namePrefix = preNameHandler(namePrefix);
        String name = oldName;
        //保留重名文件
        int index = 0;
        while (true) {
            int i = 0;
            Iterator<T> iteratorMap = ts.iterator();
            while (iteratorMap.hasNext()) {
                T tt = iteratorMap.next();
                Map<String, Object> fieldValueByNameMap = getFieldValueByName(compareField, tt);
                if (fieldValueByNameMap != null && !fieldValueByNameMap.isEmpty()) {
                    String fileName = String.valueOf(fieldValueByNameMap.get(compareField));
                    if (name.equals(fileName)) {
                        index++;
                        i = 1;
                        break;
                    }
                }

            }
            if (i != 1) {
                break;
            }
            name = namePrefix + "(" + index + ")";
            if (oldName.contains(".")) {
                name = namePrefix + "(" + index + ")." + fileType;
            }
        }
        map.put(compareField, name);
        //设置对象属性值
        setFieldsValue(t, map);
        return t;
    }

    /**
     * 文件前缀获取规则校验
     *
     * @param preFileName 前缀名
     * @return 校验后文件前缀名
     */
    public static String preNameHandler(String preFileName) {
        if (preFileName.matches(".*\\(\\d+\\)$")) {
            return preFileName.substring(0, preFileName.lastIndexOf('('));
        }
        return preFileName;
    }

    /**
     * 获取对象属性值
     *
     * @param fieldName 对象字段名
     * @param o         对象实体
     * @return 封装属性值的map
     */
    public static Map<String, Object> getFieldValueByName(String fieldName, Object o) {
        try {
            Field field = getFieldByClass(fieldName, o);
            if (null != field) {
                //设置对象的访问权限，保证对private的属性的访问
                field.setAccessible(true);
                //获取属性描述信息
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), o.getClass());
                //获取属性值读取方法
                Method readMethod = pd.getReadMethod();
                //获取属性
                Object value = readMethod.invoke(o);
                Map<String, Object> map = new HashMap<>();
                //属性值存入map
                map.put(fieldName, value);
                return map;
            }

            return null;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据属性名获取属性元素，包括各种安全范围和所有父类
     *
     * @param <T>       返回对象类型
     * @param fieldName 字段名称
     * @param object    对象本体
     * @return 返回获取的字段
     */
    public static <T> Field getFieldByClass(String fieldName, T object) {
        Field field = null;
        //获取Class类
        Class<?> clazz = object.getClass();
        //循环判断当前类是否为Object类，不是则获取字段属性，得到当前类的父类
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                //获取字段信息
                field = clazz.getDeclaredField(fieldName);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return field;
    }

    /**
     * 设置对象属性值
     *
     * @param <T> 泛型对象
     * @param t   需要设置属性的对象
     * @param map 封装属性值的map
     */
    public static <T> void setFieldsValue(T t, Map map) {
        Field f;
        try {
            Iterator<Map.Entry> entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = entries.next();
                //根据字段名获取信息
                f = getFieldByClass(String.valueOf(entry.getKey()), t);
                if (f != null) {
                    //设置继承的父类属性能被访问
                    f.setAccessible(true);
                    //设置属性值
                    f.set(t, entry.getValue());
                }
            }
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @param <T>         泛型对象
     * @param folderNames 对比对象集合
     * @param fieldName   字段名
     * @return 数字标签
     */
    public static <T> int fieldRepeat(List<T> folderNames, String fieldName) {
        int[] t = new int[folderNames.size()];
        if (t.length > 1) {
            for (int i = 0; i < folderNames.size(); i++) {
                //根据属性名获取属性值
                Map<String, Object> fieldValueByNameMap = getFieldValueByName(fieldName, folderNames.get(i));
                if (fieldValueByNameMap != null && !fieldValueByNameMap.isEmpty()) {
                    String f = (String) fieldValueByNameMap.get(fieldName);
                    //截取属性中数字
                    String fp = f.substring(f.lastIndexOf('(') + 1, f.lastIndexOf(')'));
                    t[i] = Integer.parseInt(fp);
                }
            }
            //获取最大值
            t[0] = getMax(t);
        } else {
            t[0] = 1;
        }
        return t[0] + 1;
    }

    /**
     * 获取最大值
     *
     * @param t 比较的数组
     * @return 最大值
     */
    public static int getMax(int[] t) {
        for (int j = 1; j < t.length; j++) {
            if (t[0] < t[j]) {
                int h = t[j];
                t[j] = t[0];
                t[0] = h;
            }
        }
        return t[0];
    }

    /**
     * 文件及文件夹的父文件夹id值为0和null转换
     *
     * @param t         更新字段为"0"或null
     * @param fieldName 更新文件子弹
     * @param <T>       泛型类型
     */
    public static <T> void updateParentFolderId(T t, String fieldName) {
        //根据文件字段名利用反射获取值
        Map map = GeneralUtils.getFieldValueByName(fieldName, t);
        //判断并设置字段值
        if (map != null && map.size() > 0 && "0".equals(String.valueOf(map.get(fieldName)))) {
            map.put(fieldName, null);
            GeneralUtils.setFieldsValue(t, map);
        } else if (map != null && map.size() > 0 && null == map.get(fieldName)) {
            map.put(fieldName, "0");
            GeneralUtils.setFieldsValue(t, map);
        }
    }


}
