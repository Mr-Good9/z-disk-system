package com.good.zdisksystem.service;

import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.vo.FileTreeVo;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.entity.param.RecycleBinParam;
import com.good.zdisksystem.entity.vo.UserFileVO;

public interface UserFileService {
    /**
     * 上传文件
     * @param file 文件
     * @param parentId 父文件夹ID
     * @return 文件信息
     */
    File uploadFile(MultipartFile file, Long parentId);

    /**
     * 创建文件夹
     * @param name 文件夹名
     * @param parentId 父文件夹ID
     * @return 文件夹信息
     */
    File createFolder(String name, Long parentId);

    /**
     * 获取文件列表
     * @param parentId 父文件夹ID
     * @return 文件列表
     */
    List<File> getFileList(Long parentId ,Integer isShared);

    /**
     * 获取文件夹树
     * @return 文件夹树
     */
    List<FileTreeVo> getFolderTree();

    /**
     * 重命名文件
     * @param fileId 文件ID
     * @param newName 新文件名
     */
    void renameFile(Long fileId, String newName);

    /**
     * 移动文件
     * @param fileId 文件ID
     * @param targetFolderId 目标文件夹ID
     */
    void moveFile(Long fileId, Long targetFolderId);

    /**
     * 删除文件
     * @param fileId 文件ID
     */
    void deleteFile(Long fileId);

    /**
     * 下载文件
     * @param fileId 文件ID
     * @return 文件字节数组
     */
    byte[] downloadFile(Long fileId);

    /**
     * 搜索文件
     * @param name 文件名
     * @return 文件列表
     */
    List<File> searchFiles(String name);

    String getPreviewUrl(Long fileId);

    PageResult<UserFileVO> getRecycleBinFiles(RecycleBinParam param);

    void restoreFile(Long fileId);

    void deleteFileCompletely(Long fileId);

    void clearRecycleBin();

    /**
     * 获取视频预览URL
     * @param fileId 文件ID
     * @return 视频预览URL
     */
    String getVideoPreviewUrl(Long fileId);
}
