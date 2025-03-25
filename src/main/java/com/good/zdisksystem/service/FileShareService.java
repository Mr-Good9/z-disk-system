package com.good.zdisksystem.service;

import com.good.zdisksystem.entity.model.FileShare;
import com.good.zdisksystem.entity.model.ShareStats;
import com.good.zdisksystem.entity.model.File;
import com.good.zdisksystem.entity.param.UpdateShareSettingsParam;
import com.good.zdisksystem.entity.vo.ShareFileVO;
import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.entity.param.ShareListParam;
import com.good.zdisksystem.common.result.PageResult;

import java.util.List;

public interface FileShareService {
    // 创建分享
    FileShare createShare(Long fileId, Integer expireDays, Integer shareType, boolean isShared);

    // 获取分享详情
    FileShare getShareByCode(String shareCode);

    // 获取文件的分享列表
    List<FileShare> getSharesByFileId(Long fileId);

    // 获取用户的分享列表
    List<FileShare> getSharesByUserId(Long userId);

    // 取消分享
    void cancelShare(Long shareId);

    // 获取分享的文件信息
    File getSharedFile(String shareCode);

    // 批量分享文件
    List<FileShare> batchShare(List<Long> fileIds, Integer expireDays);

    // 更新分享设置
    FileShare updateShareSettings(Long shareId, UpdateShareSettingsParam param);

    // 验证分享密码
    boolean verifySharePassword(String shareCode, String password);

    // 增加分享浏览次数
    void incrementViewCount(String shareCode);

    // 增加分享下载次数
    void incrementDownloadCount(String shareCode);

    // 获取我的分享列表
    List<ShareFileVO> getMyShares();

    // 获取收到的分享列表
    List<ShareFileVO> getReceivedShares();

    // 接收分享
    void receiveShare(String shareCode);

    // 获取分享统计信息
    ShareStats getShareStats(Long shareId);

    CommonResult<?> saveSharedFile(Long shareId, Long userId);

    /**
     * 获取分页的分享列表
     */
    PageResult<ShareFileVO> getShareList(ShareListParam param);

    // 添加新方法
    PageResult<ShareFileVO> getPublicShares(ShareListParam param);
}
