package com.good.zdisksystem.controller;

import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.result.PageResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.dto.MessageBoardDTO;
import com.good.zdisksystem.dto.MessageBoardRequestDTO;
import com.good.zdisksystem.entity.vo.RecommendationVO;
import com.good.zdisksystem.service.MessageAnalysisService;
import com.good.zdisksystem.service.MessageBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageBoardController {

    @Autowired
    private MessageBoardService messageBoardService;

    @Autowired
    private MessageAnalysisService messageAnalysisService;

    /**
     * 获取留言列表
     */

    /**
     * 获取留言列表
     */
    @GetMapping("/list")
    public CommonResult<PageResult<MessageBoardDTO>> getMessages(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "latest") String order) {
        Long userId = RequestUser.getUser().getId();
        PageResult<MessageBoardDTO> result = messageBoardService.getMessages(pageNum, pageSize, order, userId);
        return CommonResult.success(result);
    }

    /**
     * 发布留言
     */
    @PostMapping("/add")
    public CommonResult<MessageBoardDTO> addMessage(@RequestBody @Validated MessageBoardRequestDTO requestDTO) {
        Long userId = RequestUser.getUser().getId();
        MessageBoardDTO message = messageBoardService.addMessage(userId, requestDTO);
        return CommonResult.success(message);
    }

    /**
     * 回复留言
     */
    @PostMapping("/reply/{messageId}")
    public CommonResult<MessageBoardDTO> replyMessage(
            @PathVariable Long messageId,
            @RequestBody @Validated MessageBoardRequestDTO requestDTO) {
        Long userId = RequestUser.getUser().getId();
        MessageBoardDTO reply = messageBoardService.replyMessage(userId, messageId, requestDTO);
        return CommonResult.success(reply);
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like/{messageId}")
    public CommonResult<Boolean> toggleLike(
            @PathVariable Long messageId,
            @RequestParam(required = false, defaultValue = "true") Boolean like) {
        Long userId = RequestUser.getUser().getId();
        boolean success;
        if (like) {
            success = messageBoardService.likeMessage(userId, messageId);
        } else {
            success = messageBoardService.unlikeMessage(userId, messageId);
        }
        return CommonResult.success(success);
    }

    /**
     * 删除留言
     */
    @DeleteMapping("/{messageId}")
    public CommonResult<Boolean> deleteMessage(@PathVariable Long messageId) {
        Long userId = RequestUser.getUser().getId();
        boolean success = messageBoardService.deleteMessage(userId, messageId);
        return CommonResult.success(success);
    }

    /**
     * 分析留言内容并提供推荐
     */
    @PostMapping("/analyze")
    public CommonResult<List<RecommendationVO>> analyzeMessage(@RequestBody Map<String, String> payload) {
        Long userId = RequestUser.getUser().getId();
        String content = payload.get("content");
        List<RecommendationVO> recommendations = messageAnalysisService.analyzeAndRecommend(content, userId);
        return CommonResult.success(recommendations);
    }
}
