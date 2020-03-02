/*
 * FileUploadOrDownloadUtil.java
 * Created at 2020/2/27
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.utils;

import com.broadtext.ycadp.data.ac.provider.service.FileUploadOrDownloadService;
import com.broadtext.ycadp.fastdfs.FastFileStorageClient;
import com.broadtext.ycadp.fastdfs.entity.FastDFSFileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
@SuppressWarnings("all")
public class FileUploadOrDownloadUtil implements FileUploadOrDownloadService {
    /**
     * 服务器处理对象
     */
    @Autowired
    private FastFileStorageClient fileStorageClient;
    /**
     * 网盘工作目录
     */
    private String fastDFSPath = System.getProperty("user.dir");

    /**
     * @param multipartFile 文件信息
     * @return 文件唯一标识
     * @throws IOException 异常
     * @throws MyException 异常
     */
    @Transactional(rollbackFor = Exception.class)
    public String uploadSingleFile(MultipartFile multipartFile) throws IOException, MyException {
            //文件名
            String fileName = multipartFile.getOriginalFilename();
            String fileType = "";
            //判断文件名是否存在后缀，有则截取保存文件类型
            if (fileName != null && fileName.contains(".")) {
                fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
            }
            //先把文件保存到本地,内置防止高并发文件名冲突，生成UUID文件名目录
            File localFile = GeneralUtils.upLocalFile(multipartFile, fastDFSPath);
            if (localFile == null) {
                return null;
            }
            //上传文件到服务器
            FastDFSFileInfo fastDFSFileInfo = fileStorageClient.uploadFile(localFile.getPath(), fileType, new NameValuePair[]{});
            //删除本地文件
            if (localFile.exists()) {
                GeneralUtils.deleteDir(localFile, new File(fastDFSPath));
            }
        String groupName = fastDFSFileInfo.getGroupName();
        String storagePath = fastDFSFileInfo.getStoragePath();
        NameValuePair metaList = fastDFSFileInfo.getMetaList();

        return groupName+"_"+storagePath;
    }

    /**
     * fastDFS下载单个文件
     */
    public File downloadSingleFile(String fileKey,String fileName) {
        String localFilename = GeneralUtils.createdFileName(fileName, fastDFSPath);
        try {
            if (StringUtils.isNotEmpty(fileKey)) {
                String groupName = StringUtils.substringBefore(fileKey, "_");
                String storagePath = StringUtils.substringAfter(fileKey, "_");
                fileStorageClient.downloadFile(groupName,
                        storagePath, localFilename);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new File(localFilename);
    }
}
