package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.UserInfoService;
import com.cjym.videoweb.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聚合搜索（客户端）
 */
@RestController
@RequestMapping("/client/search")
@Tag(name = "聚合搜索", description = "前台UP主与视频搜索")
public class ClientSearchController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private VideoService videoService;

    @GetMapping
    @Operation(summary = "聚合搜索", description = "type=up 时按UP主昵称或uid模糊搜索，type=video 时按视频标题模糊搜索（keyword不传返回全部，视频仅已发布且至少有一集审核通过）；UP主搜索返回完整公开信息（uid/昵称/头像/签名/性别/大会员/关注数/粉丝数/累计获赞/累计播放/当前用户是否已关注），视频搜索返回前台视频卡片（含播放跳转链接与关注标记）；sortType 不传时按相关度得分降序，随 type 变化：up 时可选 fans（按粉丝数）/like（按累计获赞）/play（按累计播放），video 时可选 latest（按发布时间）/hot（按播放量）/favorite（按收藏数），均降序")
    public R<Object> search(
            @Parameter(description = "搜索类型：up（搜索UP主）/ video（搜索视频）") @RequestParam String type,
            @Parameter(description = "模糊关键词（匹配UP主昵称或uid / 视频标题）") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码，默认1") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页条数，默认10") @RequestParam(required = false) Integer size,
            @Parameter(description = "排序方式（不传按相关度得分；up：fans/like/play；video：latest/hot/favorite）") @RequestParam(required = false) String sortType) {
        if ("up".equalsIgnoreCase(type)) {
            return R.success(userInfoService.pageSearchUp(keyword, page, size, sortType));
        }
        if ("video".equalsIgnoreCase(type)) {
            return R.success(videoService.pageSearchVideo(keyword, page, size, sortType));
        }
        throw new GlobeException("搜索类型无效，必须为 up 或 video");
    }
}
