// z-disk-system/z-disk-system/src/main/java/com/good/zdisksystem/service/OperationLogService.java
package com.good.zdisksystem.service;

import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.dto.OperationLogQueryDTO;
import com.good.zdisksystem.entity.model.OperationLog;
import com.good.zdisksystem.entity.vo.OperationLogVO;
import org.springframework.web.context.request.RequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface OperationLogService {

    /**
     * 添加操作日志
     * @param userId 用户ID
     * @param module 操作模块
     * @param action 操作类型
     * @param detail 操作详情
     * @param request HTTP请求对象，用于获取IP
     */
    void addLog(Long userId, String module, String action, String detail, HttpServletRequest request);

    /**
     * 简化版添加日志，从请求上下文中获取用户信息
     * @param module 操作模块
     * @param action 操作类型
     * @param detail 操作详情
     */
    void addLog(String module, String action, String detail);

    /**
     * 获取操作日志
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<OperationLogVO> getOperationLogs(OperationLogQueryDTO queryDTO);

    /**
     * 导出操作日志
     * @param queryDTO 查询参数
     * @param response HTTP响应对象
     */
    void exportOperationLogs(OperationLogQueryDTO queryDTO, HttpServletResponse response);
}
