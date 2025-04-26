package com.good.zdisksystem.controller;

import com.good.zdisksystem.annotation.OperationLogger;
import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.param.CreateFolderParam;
import com.good.zdisksystem.entity.param.FileUploadParam;
import com.good.zdisksystem.entity.param.MoveFileParam;
import com.good.zdisksystem.entity.param.RenameFileParam;
import com.good.zdisksystem.entity.vo.FileTreeVo;
import com.good.zdisksystem.mapper.FileMapper;
import com.good.zdisksystem.service.UserFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Slf4j
public class UserFileController {

    private final UserFileService userFileService;

    private final FileMapper fileMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @OperationLogger(module = "文件", action = "新增", detail = "上传文件")
    public CommonResult<File> uploadFile(@Valid FileUploadParam param) {
        File file = userFileService.uploadFile(param.getFile(), param.getParentId());
        return CommonResult.success(file);
    }

    /**
     * 创建文件夹
     */
    @PostMapping("/folder")
    @OperationLogger(module = "文件", action = "新增", detail = "创建文件夹")
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
    @OperationLogger(module = "文件", action = "更新", detail = "重命名文件")
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
    @OperationLogger(module = "文件", action = "更新", detail = "移动文件")
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
    @OperationLogger(module = "文件", action = "删除", detail = "删除文件")
    public CommonResult<Void> deleteFile(@PathVariable Long fileId) {
        userFileService.deleteFile(fileId);
        return CommonResult.success(null);
    }

    /**
     * 下载文件
     */
    @GetMapping("/{fileId}/download")
    @OperationLogger(module = "文件", action = "下载", detail = "下载文件")
    public void downloadFile(
            @PathVariable Long fileId,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            HttpServletResponse response) {
        InputStream inputStream = null;
        ServletOutputStream outputStream = null;
        boolean responseStarted = false;

        try {
            // 获取文件信息
            File file = userFileService.checkAndGetFile(fileId);
            long fileSize = file.getSize();
            long startByte = 0;
            long endByte = fileSize - 1;

            // 处理断点续传
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String rangeValue = rangeHeader.substring("bytes=".length());
                String[] ranges = rangeValue.split("-");
                if (ranges.length > 0) {
                    startByte = Long.parseLong(ranges[0]);
                    if (ranges.length > 1 && !ranges[1].isEmpty()) {
                        endByte = Long.parseLong(ranges[1]);
                    }
                }
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + fileSize);
            }

            // 计算需要发送的字节数
            long contentLength = endByte - startByte + 1;
            
            // 获取文件流
            inputStream = userFileService.getFileInputStream(fileId);
            
            // 跳过已下载的部分
            inputStream.skip(startByte);

            // 设置响应内容类型
            String contentType = determineContentType(file.getType());
            response.setContentType(contentType);
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Length", String.valueOf(contentLength));

            // 设置内容处理方式
            String fileName = file.getName();
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);

            // 设置连接超时时间
            response.setHeader("Connection", "Keep-Alive");
            response.setHeader("Keep-Alive", "timeout=30, max=100");

            // 缓冲区设置为64KB以提高传输效率
            byte[] buffer = new byte[65536];
            int bytesRead;
            long bytesRemaining = contentLength;
            
            // 获取用户ID用于记录进度
            Long userId = RequestUser.getUser().getId();

            // 获取输出流
            outputStream = response.getOutputStream();
            responseStarted = true;

            // 复制数据，确保完整传输
            long lastProgressUpdate = System.currentTimeMillis();
            long totalBytesSent = 0;

            while (bytesRemaining > 0 && (bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                bytesRemaining -= bytesRead;
                totalBytesSent += bytesRead;
                
                // 定期刷新输出流，但不要太频繁
                if (totalBytesSent % 524288 == 0) { // 每512KB刷新一次
                    outputStream.flush();
                }
                
                // 定期更新下载进度到Redis（每2秒更新一次）
                long now = System.currentTimeMillis();
                if (now - lastProgressUpdate > 2000) {
                    saveDownloadProgress(userId, fileId, startByte + totalBytesSent, fileSize);
                    lastProgressUpdate = now;
                }
            }
            
            // 最后一次刷新确保所有数据都发送
            outputStream.flush();

            log.info("文件下载成功: fileId={}, fileName={}, range={}-{}", fileId, file.getName(), startByte, endByte);
        } catch (IOException e) {
            log.error("文件下载IO异常: fileId={}, error={}", fileId, e.getMessage(), e);
            // 客户端中断连接，不需要发送错误响应
            if (e instanceof java.net.SocketTimeoutException || 
                e instanceof java.net.SocketException ||
                e instanceof org.apache.catalina.connector.ClientAbortException) {
                log.warn("客户端可能已中断连接: fileId={}", fileId);
                // 无需处理，这是客户端行为
            } else if (!responseStarted) {
                // 只有在响应尚未开始时才尝试发送错误响应
                try {
                    response.reset();
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"code\":500,\"message\":\"下载失败: " + e.getMessage() + "\"}");
                } catch (Exception ex) {
                    log.error("无法发送错误响应", ex);
                }
            }
        } catch (Exception e) {
            log.error("文件下载失败: fileId={}, error={}", fileId, e.getMessage(), e);
            if (!responseStarted) {
                try {
                    response.reset();
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"code\":500,\"message\":\"下载失败: " + e.getMessage() + "\"}");
                } catch (Exception ex) {
                    log.error("无法发送错误响应", ex);
                }
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

    /**
     * 获取用户文件统计信息
     */
    @GetMapping("/statistics")
    public CommonResult<Map<String, Object>> getFileStatistics() {
        Long userId = RequestUser.getUser().getId();
        // 根据用户ID统计不同类型文件数量
        Map<String, Object> statistics = new HashMap<>();

        // 查询文档类型文件数量
        LambdaQueryWrapper<File> docQuery = new LambdaQueryWrapper<>();
        docQuery.eq(File::getUserId, userId)
                .eq(File::getIsDeleted, 0)
                .in(File::getType, "doc", "docx", "pdf", "txt", "xls", "xlsx", "ppt", "pptx");
        long docCount = fileMapper.selectCount(docQuery);
        statistics.put("docCount", docCount);

        // 查询图片类型文件数量
        LambdaQueryWrapper<File> imageQuery = new LambdaQueryWrapper<>();
        imageQuery.eq(File::getUserId, userId)
                 .eq(File::getIsDeleted, 0)
                 .in(File::getType, "jpg", "jpeg", "png", "gif", "bmp");
        long imageCount = fileMapper.selectCount(imageQuery);
        statistics.put("imageCount", imageCount);

        // 查询视频类型文件数量
        LambdaQueryWrapper<File> videoQuery = new LambdaQueryWrapper<>();
        videoQuery.eq(File::getUserId, userId)
                 .eq(File::getIsDeleted, 0)
                 .in(File::getType, "mp4", "avi", "mov", "wmv", "flv");
        long videoCount = fileMapper.selectCount(videoQuery);
        statistics.put("videoCount", videoCount);

        // 查询其他类型文件数量(不包括上述类型和文件夹)
        LambdaQueryWrapper<File> otherQuery = new LambdaQueryWrapper<>();
        otherQuery.eq(File::getUserId, userId)
                 .eq(File::getIsDeleted, 0)
                 .eq(File::getIsFolder, 0)
                 .notIn(File::getType,
                       "doc", "docx", "pdf", "txt", "xls", "xlsx", "ppt", "pptx",
                       "jpg", "jpeg", "png", "gif", "bmp",
                       "mp4", "avi", "mov", "wmv", "flv");
        long otherCount = fileMapper.selectCount(otherQuery);
        statistics.put("otherCount", otherCount);

        return CommonResult.success(statistics);
    }

    /**
     * 记录文件下载进度到Redis
     */
    public void saveDownloadProgress(Long userId, Long fileId, long downloaded, long total) {
        String key = "download:progress:" + userId + ":" + fileId;
        Map<String, Object> progressInfo = new HashMap<>();
        progressInfo.put("downloaded", downloaded);
        progressInfo.put("total", total);
        progressInfo.put("percentage", Math.round((double) downloaded / total * 100));
        progressInfo.put("updateTime", System.currentTimeMillis());
        
        // 设置1小时过期
        redisTemplate.opsForValue().set(key, progressInfo, 1, TimeUnit.HOURS);
    }

    /**
     * 获取文件下载进度
     */
    public Map<String, Object> getDownloadProgress(Long userId, Long fileId) {
        String key = "download:progress:" + userId + ":" + fileId;
        return (Map<String, Object>) redisTemplate.opsForValue().get(key);
    }
}
