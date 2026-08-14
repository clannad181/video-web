package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.dto.FollowSaveDto;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关注UP主（客户端）
 */
@RestController
@RequestMapping("/client/follow")
@Tag(name = "关注UP主", description = "前台用户关注/取消关注UP主")
public class ClientFollowController {

    @Resource
    private FollowService followService;

    @PostMapping("/add")
    @Operation(summary = "关注UP主", description = "关注指定UP主（需登录），已关注时幂等返回成功")
    public R<String> add(@RequestBody FollowSaveDto dto) {
        followService.follow(dto.getFollowUserId());
        return R.success("关注成功");
    }

    @PostMapping("/remove")
    @Operation(summary = "取消关注UP主", description = "取消关注指定UP主（需登录），未关注时幂等返回成功")
    public R<String> remove(@RequestBody FollowSaveDto dto) {
        followService.unfollow(dto.getFollowUserId());
        return R.success("取消关注成功");
    }

    @GetMapping("/is-followed")
    @Operation(summary = "是否已关注UP主", description = "查询当前用户是否已关注指定UP主，未登录时返回 false")
    public R<Boolean> isFollowed(@RequestParam Long followUserId) {
        return R.success(followService.isFollowed(followUserId));
    }
}
