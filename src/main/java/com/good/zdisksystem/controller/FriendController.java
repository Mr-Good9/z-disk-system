package com.good.zdisksystem.controller;

import com.good.zdisksystem.common.result.CommonResult;
import com.good.zdisksystem.common.utils.RequestUser;
import com.good.zdisksystem.entity.model.Friend;
import com.good.zdisksystem.entity.model.FriendGroup;
import com.good.zdisksystem.entity.param.AddFriendParam;
import com.good.zdisksystem.entity.vo.FriendRequestVO;
import com.good.zdisksystem.entity.vo.UserFriendVO;
import com.good.zdisksystem.entity.vo.UserVO;
import com.good.zdisksystem.service.FriendGroupService;
import com.good.zdisksystem.service.FriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "好友管理接口")
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final FriendGroupService friendGroupService;

    @ApiOperation("获取好友列表")
    @GetMapping("/all")
    public CommonResult<List<UserFriendVO>> getFriendList() {
        Long userId = RequestUser.getUser().getId();
        return CommonResult.success(friendService.getFriendList());
    }

    @ApiOperation("搜索好友")
    @GetMapping("/search")
    public CommonResult<List<UserVO>> searchFriends(@RequestParam String keyword) {
        return CommonResult.success(friendService.searchFriends(keyword));
    }

    @ApiOperation("获取好友详情")
    @GetMapping("/{friendId}")
    public CommonResult<UserFriendVO> getFriendDetail(@PathVariable Long friendId) {
        return CommonResult.success(friendService.getFriendDetail(friendId));
    }

    @ApiOperation("发送好友请求")
    @PostMapping("/request")
    public CommonResult<Void> addFriendRequest(@Valid @RequestBody AddFriendParam param) {
        friendService.addFriendRequest(param);
        return CommonResult.success(null);
    }

    @ApiOperation("获取收到的好友请求")
    @GetMapping("/request/received")
    public CommonResult<List<FriendRequestVO>> getReceivedRequests() {
        return CommonResult.success(friendService.getReceivedRequests());
    }

    @ApiOperation("获取发送的好友请求")
    @GetMapping("/request/sent")
    public CommonResult<List<FriendRequestVO>> getSentRequests() {
        return CommonResult.success(friendService.getSentRequests());
    }

    @ApiOperation("处理好友请求")
    @PostMapping("/request/{requestId}")
    public CommonResult<Void> handleFriendRequest(
            @PathVariable Long requestId,
            @RequestParam Boolean accept) {
        friendService.handleFriendRequest(requestId, accept);
        return CommonResult.success(null);
    }

    @ApiOperation("删除好友")
    @DeleteMapping("/{friendId}")
    public CommonResult<Void> deleteFriend(@PathVariable Long friendId) {
        friendService.deleteFriend(friendId);
        return CommonResult.success(null);
    }

    @ApiOperation("修改好友备注")
    @PutMapping("/{friendId}/remark")
    public CommonResult<Void> updateFriendRemark(
            @PathVariable Long friendId,
            @RequestParam String remark) {
        friendService.updateFriendRemark(friendId, remark);
        return CommonResult.success(null);
    }

    @ApiOperation("移动好友分组")
    @PutMapping("/{friendId}/group")
    public CommonResult<Void> moveFriendGroup(
            @PathVariable Long friendId,
            @RequestParam Long groupId) {
        friendService.moveFriendGroup(friendId, groupId);
        return CommonResult.success(null);
    }

    @ApiOperation("获取好友分组列表")
    @GetMapping("/groups")
    public CommonResult<List<FriendGroup>> getFriendGroups() {
        Long userId = RequestUser.getUser().getId();
        return CommonResult.success(friendService.getFriendGroups(userId));
    }

    @ApiOperation("获取指定分组的好友列表")
    @GetMapping("/group/{groupId}")
    public CommonResult<List<Friend>> getFriendsByGroup(@PathVariable Long groupId) {
        Long userId = RequestUser.getUser().getId();
        return CommonResult.success(friendService.getFriendsByGroup(userId, groupId));
    }

    @ApiOperation("创建好友分组")
    @PostMapping("/group")
    public CommonResult<FriendGroup> createGroup(@RequestParam String name) {
        return CommonResult.success(friendGroupService.createGroup(name));
    }

    @ApiOperation("更新分组名称")
    @PutMapping("/group/{groupId}/name")
    public CommonResult<Void> updateGroupName(
            @PathVariable Long groupId,
            @RequestParam String newName) {
        friendGroupService.updateGroupName(groupId, newName);
        return CommonResult.success(null);
    }

    @ApiOperation("更新分组排序")
    @PutMapping("/group/{groupId}/sort")
    public CommonResult<Void> updateGroupSort(
            @PathVariable Long groupId,
            @RequestParam Integer sort) {
        friendGroupService.updateGroupSort(groupId, sort);
        return CommonResult.success(null);
    }

    @ApiOperation("删除好友分组")
    @DeleteMapping("/group/{groupId}")
    public CommonResult<Void> deleteGroup(@PathVariable Long groupId) {
        friendGroupService.deleteGroup(groupId);
        return CommonResult.success(null);
    }
}
