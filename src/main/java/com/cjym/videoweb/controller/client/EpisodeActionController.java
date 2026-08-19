package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.vo.EpisodeActionStatusVo;
import com.cjym.videoweb.model.vo.EpisodeActionVo;
import com.cjym.videoweb.model.vo.EpisodeFavoriteStatusVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.EpisodeActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台剧集互动（点赞/投币）
 */
@RestController
@RequestMapping("/client/episode")
@Tag(name = "剧集互动（前台）", description = "前台视频剧集点赞与投币接口")
public class EpisodeActionController {

    @Resource
    private EpisodeActionService episodeActionService;

    /**
     * 点赞/取消点赞剧集
     *
     * @param episodeUid 剧集avid
     */
    @PostMapping("/{episodeUid}/like")
    @Operation(summary = "点赞/取消点赞", description = "toggle逻辑：已点赞则取消，未点赞则点赞；返回最新点赞数和当前用户点赞状态")
    public R<EpisodeActionVo> like(
            @Parameter(description = "剧集avid") @PathVariable String episodeUid) {
        return R.success(episodeActionService.toggleLike(episodeUid));
    }

    /**
     * 投币（每个用户每集只能投币一次，不可取消）
     *
     * @param episodeUid 剧集avid
     */
    @PostMapping("/{episodeUid}/coin")
    @Operation(summary = "投币", description = "每个用户每集只能投币一次，不可取消；返回最新投币数和当前用户投币状态")
    public R<EpisodeActionVo> coin(
            @Parameter(description = "剧集avid") @PathVariable String episodeUid) {
        return R.success(episodeActionService.coin(episodeUid));
    }

    /**
     * 查询当前用户对指定剧集的点赞和投币状态
     *
     * @param episodeUid 剧集avid
     */
    @GetMapping("/{episodeUid}/status")
    @Operation(summary = "查询互动状态", description = "查询当前用户对该剧集是否已点赞、是否已投币；未登录时全部返回false")
    public R<EpisodeActionStatusVo> status(
            @Parameter(description = "剧集avid") @PathVariable String episodeUid) {
        return R.success(episodeActionService.getActionStatus(episodeUid));
    }

    /**
     * 收藏/取消收藏剧集
     *
     * @param episodeUid 剧集avid
     */
    @PostMapping("/{episodeUid}/favorite")
    @Operation(summary = "收藏/取消收藏", description = "toggle逻辑：已收藏则取消，未收藏则收藏；返回最新收藏数和当前用户收藏状态")
    public R<EpisodeActionVo> favorite(
            @Parameter(description = "剧集avid") @PathVariable String episodeUid) {
        return R.success(episodeActionService.toggleFavorite(episodeUid));
    }

    /**
     * 查询当前用户对指定剧集的收藏状态
     *
     * @param episodeUid 剧集avid
     */
    @GetMapping("/{episodeUid}/favorite/status")
    @Operation(summary = "查询收藏状态", description = "查询当前用户对该剧集是否已收藏；未登录时返回false")
    public R<EpisodeFavoriteStatusVo> favoriteStatus(
            @Parameter(description = "剧集avid") @PathVariable String episodeUid) {
        return R.success(episodeActionService.getFavoriteStatus(episodeUid));
    }
}
