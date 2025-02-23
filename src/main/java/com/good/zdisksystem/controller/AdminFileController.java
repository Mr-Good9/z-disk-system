package com.good.zdisksystem.controller;

import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.entity.vo.FileStatisticsVO;
import com.good.zdisksystem.entity.param.FileQueryParam;
import com.good.zdisksystem.model.vo.FileVO;
import com.good.zdisksystem.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/files")
public class AdminFileController {

    @Autowired
    private FileService fileService;

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
} 