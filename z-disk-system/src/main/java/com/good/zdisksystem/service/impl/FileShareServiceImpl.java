package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.model.FileShare;
import com.good.zdisksystem.mapper.FileShareMapper;
import com.good.zdisksystem.service.IFileShareService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件分享表 服务实现类
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Service
public class FileShareServiceImpl extends ServiceImpl<FileShareMapper, FileShare> implements IFileShareService {

}
