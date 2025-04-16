package com.good.zdisksystem.controller;

import com.good.zdisksystem.annotation.OperationLogger;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.vo.FileStatisticsVO;
import com.good.zdisksystem.entity.param.FileQueryParam;
import com.good.zdisksystem.entity.vo.FileVO;
import com.good.zdisksystem.service.FileService;
import com.good.zdisksystem.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/api/admin/files")
public class AdminFileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private MinioService minioService;

    /**
     * 获取文件统计信息
     */
    @GetMapping("/statistics")
    public CommonResult<FileStatisticsVO> getFileStatistics() {
        FileStatisticsVO statistics = fileService.getFileStatistics();
        return CommonResult.success(statistics);
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    public CommonResult<PageResult<FileVO>> getFileList(@Valid FileQueryParam param) {
        PageResult<FileVO> result = fileService.getFileList(param);
        return CommonResult.success(result);
    }

    /**
     * 批量删除文件
     */
    @DeleteMapping("/batch")
    @OperationLogger(module = "管理员", action = "删除", detail = "批量删除文件")
    public CommonResult<Void> batchDeleteFiles(@RequestBody List<Long> fileIds) {
        fileService.batchDeleteFiles(fileIds);
        return CommonResult.success(null);
    }

    /**
     * 获取文件预览链接
     */
    @GetMapping("/{fileId}/preview")
    public CommonResult<String> getPreviewUrl(@PathVariable Long fileId) {
        String url = fileService.getPreviewUrl(fileId);
        return CommonResult.success(url);
    }

    @GetMapping("/{fileId}/download")
    @OperationLogger(module = "管理员", action = "下载", detail = "下载文件")
    public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) throws IOException {
        // 获取文件信息
        File file = fileService.getById(fileId);
        if (file == null) {
            throw new CustomException("文件不存在");
        }

        // 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));

        // 从MinIO获取文件并写入响应
        try (InputStream inputStream = minioService.getFileInputStream(file.getPath());
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        }
    }
}
