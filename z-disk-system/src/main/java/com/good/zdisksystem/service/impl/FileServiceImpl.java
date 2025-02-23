package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.model.File;
import com.good.zdisksystem.mapper.FileMapper;
import com.good.zdisksystem.service.IFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 文件表 服务实现类
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

}
