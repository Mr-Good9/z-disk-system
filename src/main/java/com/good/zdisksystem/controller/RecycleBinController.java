package com.good.zdisksystem.controller;

import com.good.zdisksystem.annotation.OperationLogger;
import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.entity.param.RecycleBinParam;
import com.good.zdisksystem.entity.vo.UserFileVO;
import com.good.zdisksystem.service.UserFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@Api(tags = "回收站接口")
@RestController
@RequestMapping("/api/recycle")
@RequiredArgsConstructor
public class RecycleBinController {

    private static final Logger log = LoggerFactory.getLogger(RecycleBinController.class);

    private final UserFileService userFileService;

    @ApiOperation("获取回收站文件列表")
    @GetMapping("/list")
    public CommonResult<PageResult<UserFileVO>> getRecycleBinFiles(RecycleBinParam param) {
        log.info("获取回收站文件列表: param={}", param);
        PageResult<UserFileVO> result = userFileService.getRecycleBinFiles(param);
        return CommonResult.success(result);
    }

    @ApiOperation("恢复文件")
    @PostMapping("/{fileId}/restore")
    @OperationLogger(module = "文件", action = "更新", detail = "恢复文件")
    public CommonResult<Void> restoreFile(@PathVariable Long fileId) {
        userFileService.restoreFile(fileId);
        return CommonResult.success(null);
    }

    @ApiOperation("彻底删除文件")
    @DeleteMapping("/{fileId}")
    @OperationLogger(module = "文件", action = "删除", detail = "彻底删除文件")
    public CommonResult<Void> deleteFileCompletely(@PathVariable Long fileId) {
        userFileService.deleteFileCompletely(fileId);
        return CommonResult.success(null);
    }

    @ApiOperation("清空回收站")
    @DeleteMapping("/clear")
    @OperationLogger(module = "文件", action = "删除", detail = "清空回收站")
    public CommonResult<Void> clearRecycleBin() {
        userFileService.clearRecycleBin();
        return CommonResult.success(null);
    }
}
