package com.good.zdisksystem.controller;

import cn.hutool.core.bean.BeanUtil;
import com.good.zdisksystem.common.exception.CustomException;
import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.entity.model.FileShare;
import com.good.zdisksystem.entity.model.ShareStats;
import com.good.zdisksystem.entity.param.BatchShareParam;
import com.good.zdisksystem.entity.param.ShareFileParam;
import com.good.zdisksystem.entity.param.UpdateShareSettingsParam;
import com.good.zdisksystem.entity.param.VerifySharePasswordParam;
import com.good.zdisksystem.entity.param.ShareListParam;
import com.good.zdisksystem.entity.vo.ShareFileVO;
import com.good.zdisksystem.service.FileShareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "文件分享接口")
@Validated
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class FileShareController {

    @Autowired
    private FileShareService fileShareService;

    @ApiOperation("创建分享")
    @PostMapping
    public CommonResult<FileShare> createShare(@Valid @RequestBody ShareFileParam param) {
        FileShare share = fileShareService.createShare(
            param.getFileId(),
            param.getExpireDays(),
            param.getShareType()
        );
        return CommonResult.success(share);
    }

    @ApiOperation("批量分享")
    @PostMapping("/batch")
    public CommonResult<List<FileShare>> batchShare(@Valid @RequestBody BatchShareParam param) {
        List<FileShare> shares = fileShareService.batchShare(param.getFileIds(), param.getExpireDays());
        return CommonResult.success(shares);
    }

    @ApiOperation("获取分享列表")
    @GetMapping("/list")
    public CommonResult<PageResult<ShareFileVO>> getShareList(ShareListParam param) {
        PageResult<ShareFileVO> result = fileShareService.getShareList(param);
        return CommonResult.success(result);
    }

    @ApiOperation("获取分享统计信息")
    @GetMapping("/stats/{fileId}")
    public CommonResult<ShareStats> getShareStats(
            @ApiParam(value = "文件ID", required = true)
            @PathVariable @NotNull(message = "文件ID不能为空") Long fileId) {
        ShareStats stats = fileShareService.getShareStats(fileId);
        return CommonResult.success(stats);
    }

    @ApiOperation("更新分享设置")
    @PutMapping("/{shareId}/settings")
    public CommonResult<FileShare> updateShareSettings(
            @ApiParam(value = "分享ID", required = true)
            @PathVariable @NotNull(message = "分享ID不能为空") Long shareId,
            @Valid @RequestBody UpdateShareSettingsParam param) {
        FileShare share = fileShareService.updateShareSettings(shareId, param);
        return CommonResult.success(share);
    }

    @ApiOperation("取消分享")
    @DeleteMapping("/{shareId}")
    public CommonResult<Void> cancelShare(@PathVariable Long shareId) {
        fileShareService.cancelShare(shareId);
        return CommonResult.success(null);
    }

    @ApiOperation("获取分享文件信息")
    @GetMapping("/{shareCode}")
    public CommonResult<ShareFileVO> getShareInfo(
            @PathVariable String shareCode,
            @RequestParam(required = false) String code) {
        FileShare share = fileShareService.getShareByCode(shareCode);

        // 检查分享是否有效
        if (share == null || share.getIsDeleted() == 1) {
            throw new CustomException("分享不存在或已被删除");
        }

        if (share.getStatus() != 0) {
            throw new CustomException("分享已失效");
        }

        // 检查访问密码
        if (share.getShareType() == 0) {  // 私密分享
            if (share.getAccessCode() != null) {
                if (code == null || !share.getAccessCode().equals(code)) {
                    throw new CustomException("访问密码错误");
                }
            }
        }

        // 转换为 VO
        ShareFileVO vo = BeanUtil.toBean(share, ShareFileVO.class);
        vo.setNeedPassword(share.getShareType() == 0 && share.getAccessCode() != null);

        return CommonResult.success(vo);
    }

    @ApiOperation("验证分享密码")
    @PostMapping("/{shareCode}/verify")
    public CommonResult<Boolean> verifySharePassword(
            @ApiParam(value = "分享码", required = true)
            @PathVariable @NotBlank(message = "分享码不能为空") String shareCode,
            @Valid @RequestBody VerifySharePasswordParam param) {
        boolean valid = fileShareService.verifySharePassword(shareCode, param.getPassword());
        return CommonResult.success(valid);
    }

    @ApiOperation("增加分享浏览次数")
    @PostMapping("/{shareCode}/view")
    public CommonResult<Void> incrementViewCount(
            @ApiParam(value = "分享码", required = true)
            @PathVariable @NotBlank(message = "分享码不能为空") String shareCode) {
        fileShareService.incrementViewCount(shareCode);
        return CommonResult.success(null);
    }

    @ApiOperation("增加分享下载次数")
    @PostMapping("/{shareCode}/download")
    public CommonResult<Void> incrementDownloadCount(
            @ApiParam(value = "分享码", required = true)
            @PathVariable @NotBlank(message = "分享码不能为空") String shareCode) {
        fileShareService.incrementDownloadCount(shareCode);
        return CommonResult.success(null);
    }

    @ApiOperation("保存分享文件到我的网盘")
    @PostMapping("/{shareId}/save")
    public CommonResult<?> saveSharedFile(@PathVariable Long shareId) {
        // 从 SecurityContext 中获取当前用户ID
        Long userId = RequestUser.getUser().getId();
        return fileShareService.saveSharedFile(shareId, userId);
    }
}
