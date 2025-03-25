package com.good.zdisksystem.service;

import java.io.InputStream;

public interface MinioService {
    /**
     * 获取文件预览URL
     */
    String getPreviewUrl(String path);

    /**
     * 获取文件输入流
     */
    InputStream getFileInputStream(String path);

    /**
     * 上传文件
     */
    String uploadFile(byte[] fileData, String fileName);

    /**
     * 删除文件
     */
    void deleteFile(String path);

    /**
     * 复制文件对象
     * @param sourceObjectName 源对象名称
     * @param targetObjectName 目标对象名称
     * @throws Exception 复制过程中可能发生的异常
     */
    void copyObject(String sourceObjectName, String targetObjectName) throws Exception;
}
