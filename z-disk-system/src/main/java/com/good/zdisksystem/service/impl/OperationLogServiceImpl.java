package com.good.zdisksystem.service.impl;

import com.good.zdisksystem.model.OperationLog;
import com.good.zdisksystem.mapper.OperationLogMapper;
import com.good.zdisksystem.service.IOperationLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志表 服务实现类
 * </p>
 *
 * @author chris
 * @since 2025-02-16
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements IOperationLogService {

}
