/*
 * FileUploadOrDownloadService.java
 * Created at 2020/2/28
 * Created by ouhaoliang
 * Copyright (C) 2020 Broadtext, All rights reserved
 */

package com.broadtext.ycadp.data.ac.provider.service;

import org.csource.common.MyException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface FileUploadOrDownloadService {
    /**
     * 文件上传接口
     * @param multipartFile multipartFile 文件信息
     * @return 文件唯一标识
     * @throws IOException 异常
     * @throws MyException 异常
     */
    String uploadSingleFile(MultipartFile multipartFile) throws IOException, MyException;

    /**
     * 文件下载接口
     * @param fileKey 文件唯一标识
     * @param fileName 文件名
     * @return 文件
     */
    File downloadSingleFile(String fileKey, String fileName);

}
