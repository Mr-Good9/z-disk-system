// z-disk-system/z-disk-system/src/main/java/com/good/zdisksystem/controller/OperationLogController.java
package com.good.zdisksystem.controller;

import com.good.zdisksystem.annotation.OperationLogger;
import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.dto.OperationLogQueryDTO;
import com.good.zdisksystem.entity.vo.OperationLogVO;
import com.good.zdisksystem.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Api(tags = "操作日志管理")
@RestController
@RequestMapping("/api/admin/operation-logs")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @ApiOperation("获取操作日志列表")
    @GetMapping("")
    public CommonResult<PageResult<OperationLogVO>> getOperationLogs(
            @ApiParam("用户ID或用户名") @RequestParam(required = false) String userId,
            @ApiParam("操作模块") @RequestParam(required = false) String module,
            @ApiParam("操作类型") @RequestParam(required = false) String action,
            @ApiParam("开始时间") @RequestParam(required = false) String startTime,
            @ApiParam("结束时间") @RequestParam(required = false) String endTime,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        OperationLogQueryDTO queryDTO = new OperationLogQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setModule(module);
        queryDTO.setAction(action);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        PageResult<OperationLogVO> result = operationLogService.getOperationLogs(queryDTO);
        return CommonResult.success(result);
    }

    @ApiOperation("导出操作日志")
    @GetMapping("/export")
    @OperationLogger(module = "管理员", action = "导出", detail = "导出操作日志")
    public void exportOperationLogs(
            @ApiParam("用户ID或用户名") @RequestParam(required = false) String userId,
            @ApiParam("操作模块") @RequestParam(required = false) String module,
            @ApiParam("操作类型") @RequestParam(required = false) String action,
            @ApiParam("开始时间") @RequestParam(required = false) String startTime,
            @ApiParam("结束时间") @RequestParam(required = false) String endTime,
            HttpServletResponse response
    ) {
        OperationLogQueryDTO queryDTO = new OperationLogQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setModule(module);
        queryDTO.setAction(action);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);

        operationLogService.exportOperationLogs(queryDTO, response);
    }
}
