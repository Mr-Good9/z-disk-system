package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioServiceImpl implements MinioService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // 确保 bucket 存在
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error("初始化 MinIO 客户端失败", e);
            throw new RuntimeException("初始化 MinIO 客户端失败", e);
        }
    }

    @Override
    public String getPreviewUrl(String path) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(path)
                    .expiry(1, TimeUnit.HOURS)
                    .build()
            );
        } catch (Exception e) {
            log.error("获取文件预览链接失败", e);
            throw new RuntimeException("获取文件预览链接失败", e);
        }
    }

    @Override
    public InputStream getFileInputStream(String path) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build()
            );
        } catch (Exception e) {
            log.error("获取文件输入流失败", e);
            throw new RuntimeException("获取文件输入流失败", e);
        }
    }

    @Override
    public String uploadFile(byte[] fileData, String fileName) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(new ByteArrayInputStream(fileData), fileData.length, -1)
                    .build()
            );
            return fileName;
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new RuntimeException("上传文件失败", e);
        }
    }

    @Override
    public void deleteFile(String path) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build()
            );
        } catch (Exception e) {
            log.error("删除文件失败", e);
            throw new RuntimeException("删除文件失败", e);
        }
    }

    @Override
    public void copyObject(String sourceObjectName, String targetObjectName) throws Exception {
        try {
            minioClient.copyObject(
                CopyObjectArgs.builder()
                    .source(CopySource.builder()
                        .bucket(bucketName)
                        .object(sourceObjectName)
                        .build())
                    .bucket(bucketName)
                    .object(targetObjectName)
                    .build()
            );
        } catch (Exception e) {
            throw new Exception("复制文件失败: " + e.getMessage());
        }
    }
} 