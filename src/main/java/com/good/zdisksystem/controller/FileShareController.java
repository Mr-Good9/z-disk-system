package com.good.zdisksystem.controller;

import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.model.User;
import com.good.zdisksystem.entity.vo.FileVO;
import com.good.zdisksystem.entity.vo.ShareFileVO;
import com.good.zdisksystem.service.FileService;
import com.good.zdisksystem.service.FileShareService;
import com.good.zdisksystem.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "文件共享接口")
@Validated
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class FileShareController {

    private final FileService fileService;

    @ApiOperation("设置文件共享状态")
    @PostMapping("/{fileId}/toggle")
    public CommonResult<Boolean> toggleFileShareStatus(
            @PathVariable Long fileId,
            @RequestParam(defaultValue = "true") boolean shared) {

        // 获取当前用户
        Long userId = RequestUser.getUser().getId();

        // 更新文件共享状态
        boolean result = fileService.updateFileShareStatus(fileId, userId, shared);
        return CommonResult.success(result);
    }

    @ApiOperation("获取公共共享文件列表")
    @GetMapping("/public")
    public CommonResult<PageResult<FileVO>> getPublicSharedFiles(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword) {

        // 查询所有共享文件
        PageResult<FileVO> result = fileService.getSharedFiles(page, size, keyword);
        return CommonResult.success(result);
    }

    @ApiOperation("保存共享文件到我的文件")
    @PostMapping("/{fileId}/save")
    public CommonResult<Boolean> saveSharedFile(@PathVariable Long fileId) {
        // 获取当前用户
        Long userId = RequestUser.getUser().getId();

        // 保存文件
        boolean result = fileService.copyFileTo(fileId, userId);
        return CommonResult.success(result);
    }

    @GetMapping("/recommend")
    public CommonResult<List<FileVO>> getRecommendedShares() {
        // 获取当前用户ID
        Long userId = RequestUser.getUser().getId();
        // 调用服务获取推荐
        List<FileVO> recommendedShares = fileService.getRecommendedShares(userId);
        return CommonResult.success(recommendedShares);
    }
}
