package com.good.zdisksystem.controller;

import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.param.CreateFolderParam;
import com.good.zdisksystem.entity.param.FileUploadParam;
import com.good.zdisksystem.entity.param.MoveFileParam;
import com.good.zdisksystem.entity.param.RenameFileParam;
import com.good.zdisksystem.entity.vo.FileTreeVo;
import com.good.zdisksystem.service.UserFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class UserFileController {

    private final UserFileService userFileService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public CommonResult<File> uploadFile(@Valid FileUploadParam param) {
        File file = userFileService.uploadFile(param.getFile(), param.getParentId());
        return CommonResult.success(file);
    }

    /**
     * 创建文件夹
     */
    @PostMapping("/folder")
    public CommonResult<File> createFolder(@Valid @RequestBody CreateFolderParam param) {
        File folder = userFileService.createFolder(param.getName(), param.getParentId());
        return CommonResult.success(folder);
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    public CommonResult<List<File>> getFileList(@RequestParam(required = false) Long parentId, @RequestParam(required = false) Integer isShared) {
        List<File> files = userFileService.getFileList(parentId, isShared);
        return CommonResult.success(files);
    }

    /**
     * 获取文件夹树
     */
    @GetMapping("/folder/tree")
    public CommonResult<List<FileTreeVo>> getFolderTree() {
        List<FileTreeVo> tree = userFileService.getFolderTree();
        return CommonResult.success(tree);
    }

    /**
     * 重命名文件
     */
    @PutMapping("/{fileId}/name")
    public CommonResult<Void> renameFile(
            @PathVariable Long fileId,
            @Valid @RequestBody RenameFileParam param) {
        userFileService.renameFile(fileId, param.getNewName());
        return CommonResult.success(null);
    }

    /**
     * 移动文件
     */
    @PutMapping("/{fileId}/move")
    public CommonResult<Void> moveFile(
            @PathVariable Long fileId,
            @Valid @RequestBody MoveFileParam param) {
        userFileService.moveFile(fileId, param.getTargetFolderId());
        return CommonResult.success(null);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public CommonResult<Void> deleteFile(@PathVariable Long fileId) {
        userFileService.deleteFile(fileId);
        return CommonResult.success(null);
    }

    /**
     * 下载文件
     */
    @GetMapping("/{fileId}/download")
    public void downloadFile(
            @PathVariable Long fileId,
            HttpServletResponse response) {
        byte[] fileData = userFileService.downloadFile(fileId);
        // TODO: 设置响应头和写入文件数据
    }

    /**
     * 搜索文件
     */
    @GetMapping("/search")
    public CommonResult<List<File>> searchFiles(@RequestParam String name) {
        List<File> files = userFileService.searchFiles(name);
        return CommonResult.success(files);
    }

    @GetMapping("/{fileId}/preview")
    public CommonResult<String> getPreviewUrl(@PathVariable Long fileId) {
        String previewUrl = userFileService.getPreviewUrl(fileId);
        CommonResult<String> success = CommonResult.success(null);
        success.setData(previewUrl);
        return success;
    }

    /**
     * 获取视频预览URL
     */
    @GetMapping("/{fileId}/video-preview")
    public CommonResult<String> getVideoPreviewUrl(@PathVariable Long fileId) {
        String previewUrl = userFileService.getVideoPreviewUrl(fileId);
        return CommonResult.success(previewUrl);
    }
}
