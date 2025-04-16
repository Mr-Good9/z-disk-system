package com.good.zdisksystem.controller;

import com.good.zdisksystem.annotation.OperationLogger;
import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.dto.MessageBoardManagerDTO;
import com.good.zdisksystem.entity.vo.MessageBoardManagerDetailVO;
import com.good.zdisksystem.service.MessageBoardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 留言板管理控制器
 * @author chris
 * @since 2025/4/16 15:21
 */
@RestController
@RequestMapping("/api/admin/message")
@Api(tags = "留言板管理接口")
public class MessageManagerController {
    @Autowired
    private MessageBoardService messageBoardService;

    @GetMapping("/list")
    @ApiOperation("获取留言列表")
    public CommonResult<PageResult<MessageBoardManagerDTO>> getMessages(
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize,
            @RequestParam(required = false) @ApiParam("关键词") String keyword,
            @RequestParam(required = false) @ApiParam("状态") Integer status,
            @RequestParam(required = false) @ApiParam("开始时间") String startTime,
            @RequestParam(required = false) @ApiParam("结束时间") String endTime) {
        return CommonResult.success(messageBoardService.getManagerMessageList(pageNum, pageSize, keyword, status, startTime, endTime));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除留言")
    @OperationLogger(module = "管理员", action = "删除", detail = "删除留言")
    public CommonResult<?> deleteMessage(@PathVariable @ApiParam("留言ID") Long id) {
        messageBoardService.adminDeleteMessage(id);
        return CommonResult.success("删除成功");
    }

    @PutMapping("/{id}/restore")
    @ApiOperation("恢复留言")
    @OperationLogger(module = "管理员", action = "恢复", detail = "恢复留言")
    public CommonResult<?> restoreMessage(@PathVariable @ApiParam("留言ID") Long id) {
        messageBoardService.adminRestoreMessage(id);
        return CommonResult.success("恢复成功");
    }

    @DeleteMapping("/batch")
    @ApiOperation("批量删除留言")
    @OperationLogger(module = "管理员", action = "批量删除", detail = "批量删除留言")
    public CommonResult<?> batchDelete(@RequestBody @ApiParam("留言ID列表") List<Long> ids) {
        messageBoardService.adminBatchDelete(ids);
        return CommonResult.success("批量删除成功");
    }

    @PutMapping("/batch/restore")
    @ApiOperation("批量恢复留言")
    @OperationLogger(module = "管理员", action = "批量恢复", detail = "批量恢复留言")
    public CommonResult<?> batchRestore(@RequestBody @ApiParam("留言ID列表") List<Long> ids) {
        messageBoardService.adminBatchRestore(ids);
        return CommonResult.success("批量恢复成功");
    }

    @GetMapping("/{id}/detail")
    @ApiOperation("获取留言详情")
    public CommonResult<MessageBoardManagerDetailVO> getMessageDetail(@PathVariable @ApiParam("留言ID") Long id) {
        return CommonResult.success(messageBoardService.getManagerMessageDetail(id));
    }
}
