package com.cjym.videoweb.controller.client;

import com.cjym.videoweb.model.vo.ClientVideoDetailVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台视频播放
 */
@RestController
@RequestMapping("/client/video")
@Tag(name = "视频播放（前台）", description = "前台视频播放页接口，路径与 /admin/video 完全隔离")
public class ClientVideoPlayController {

    @Resource
    private VideoService videoService;

    /**
     * 根据视频avid和剧集avid播放指定集数
     *
     * @param videoUid   视频avid（如 av1a2b3C）
     * @param episodeUid 剧集avid（如 avXyz789）
     */
    @GetMapping("/{videoUid}/episode/{episodeUid}")
    @Operation(summary = "视频播放详情", description = "根据视频avid和剧集avid返回播放详情（视频信息+审核通过剧集列表+当前剧集+UP主信息），仅限已发布视频")
    public R<ClientVideoDetailVo> play(
            @Parameter(description = "视频avid") @PathVariable String videoUid,
            @Parameter(description = "剧集avid") @PathVariable String episodeUid) {
        return R.success(videoService.getClientPlayDetail(videoUid, episodeUid));
    }
}
