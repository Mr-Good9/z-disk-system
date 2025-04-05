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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Slf4j
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
        InputStream inputStream = null;
        ServletOutputStream outputStream = null;

        try {
            // 获取文件信息
            File file = userFileService.checkAndGetFile(fileId);

            // 获取文件流
            inputStream = userFileService.getFileInputStream(fileId);

            // 设置响应内容类型
            String contentType = determineContentType(file.getType());
            response.setContentType(contentType);

            // 设置内容处理方式
            String fileName = file.getName();
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);

            // 不设置具体的Content-Length，让服务器使用分块传输
            response.setHeader("Transfer-Encoding", "chunked");

            // 缓冲区设置为8KB
            byte[] buffer = new byte[8192];
            int bytesRead;

            // 获取输出流
            outputStream = response.getOutputStream();

            // 复制数据，确保完整传输
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush(); // 确保每个数据块都被发送
            }

            log.info("文件下载成功: fileId={}, fileName={}", fileId, file.getName());
        } catch (Exception e) {
            log.error("文件下载失败: fileId={}, error={}", fileId, e.getMessage(), e);
            try {
                response.reset(); // 重置响应
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"code\":500,\"message\":\"" + e.getMessage() + "\"}");
            } catch (IOException ex) {
                // 忽略写入错误响应时的异常
                log.error("无法发送错误响应", ex);
            }
        } finally {
            // 确保资源正确关闭
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭输入流失败", e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("关闭输出流失败", e);
                }
            }
        }
    }

    /**
     * 根据文件扩展名确定内容类型
     */
    private String determineContentType(String fileExtension) {
        if (fileExtension == null) {
            return "application/octet-stream";
        }

        fileExtension = fileExtension.toLowerCase();

        switch (fileExtension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            case "html":
            case "htm":
                return "text/html";
            case "mp3":
                return "audio/mpeg";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "wmv":
                return "video/x-ms-wmv";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            case "7z":
                return "application/x-7z-compressed";
            default:
                return "application/octet-stream";
        }
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
