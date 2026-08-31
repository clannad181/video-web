package com.cjym.videoweb.controller.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.FollowListDto;
import com.cjym.videoweb.model.dto.FollowSaveDto;
import com.cjym.videoweb.model.vo.FansListVo;
import com.cjym.videoweb.model.vo.FollowListVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
        followService.follow(dto.getFollowUserUid());
        return R.success("关注成功");
    }

    @PostMapping("/remove")
    @Operation(summary = "取消关注UP主", description = "取消关注指定UP主（需登录），未关注时幂等返回成功")
    public R<String> remove(@RequestBody FollowSaveDto dto) {
        followService.unfollow(dto.getFollowUserUid());
        return R.success("取消关注成功");
    }

    @GetMapping("/is-followed")
    @Operation(summary = "是否已关注UP主", description = "查询当前用户是否已关注指定UP主，未登录时返回 false")
    public R<Boolean> isFollowed(@RequestParam String followUserUid) {
        return R.success(followService.isFollowed(followUserUid));
    }

    /**
     * 分页查询关注列表
     *
     * @param page 页码，从1开始（默认1）
     * @param size 每页数量（默认10）
     */
    @GetMapping("/list")
    @Operation(summary = "获取关注的UP主列表", description = "分页查询当前用户关注的UP主列表（需登录），含UP主完整公开信息（uid/昵称/头像/签名/性别/大会员/关注数/粉丝数/累计获赞/累计播放）与关注时间，按关注时间倒序")
    public R<IPage<FollowListVo>> list(
            @Parameter(description = "页码，从1开始（默认1）") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量（默认10）") @RequestParam(required = false) Integer size) {
        FollowListDto dto = new FollowListDto();
        dto.setPage(page);
        dto.setSize(size);
        return R.success(followService.pageList(dto));
    }

    /**
     * 模糊搜索已关注的UP主
     *
     * @param keyword 模糊关键词（可选，匹配UP主昵称或uid，不传返回全部）
     * @param page    页码，从1开始（默认1）
     * @param size    每页数量（默认10）
     */
    @GetMapping("/search")
    @Operation(summary = "模糊搜索已关注的UP主", description = "按UP主昵称或uid模糊搜索当前用户已关注的UP主（需登录），keyword不传时返回全部关注；返回UP主完整公开信息与关注时间，按关注时间倒序")
    public R<IPage<FollowListVo>> search(
            @Parameter(description = "模糊关键词（匹配UP主昵称或uid）") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码，从1开始（默认1）") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量（默认10）") @RequestParam(required = false) Integer size) {
        return R.success(followService.pageSearch(keyword, page, size));
    }

    /**
     * 模糊搜索粉丝列表
     *
     * @param keyword 模糊关键词（可选，匹配粉丝昵称或uid，不传返回全部）
     * @param page    页码，从1开始（默认1）
     * @param size    每页数量（默认10）
     */
    @GetMapping("/fans")
    @Operation(summary = "获取粉丝列表", description = "按昵称或uid模糊搜索当前用户的粉丝列表（需登录），keyword不传时返回全部；返回粉丝完整公开信息、是否已回关与关注时间，按关注时间倒序")
    public R<IPage<FansListVo>> fans(
            @Parameter(description = "模糊关键词（匹配粉丝昵称或uid）") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码，从1开始（默认1）") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量（默认10）") @RequestParam(required = false) Integer size) {
        return R.success(followService.pageFans(keyword, page, size));
    }
}
