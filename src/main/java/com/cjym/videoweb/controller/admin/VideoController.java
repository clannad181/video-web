package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.VideoEpisodeSaveBatchDto;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.dto.VideoSaveDto;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.vo.CategoryStatsVo;
import com.cjym.videoweb.model.vo.VideoEpisodeVo;
import com.cjym.videoweb.model.vo.VideoReviewStatsVo;
import com.cjym.videoweb.model.vo.VideoVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/video")
@Tag(name = "视频管理", description = "视频管理")
public class VideoController {

    @Resource
    private VideoService videoService;

    @GetMapping("/list")
    @Operation(summary = "分页查询视频列表", description = "支持按标题模糊搜索、分类、审核状态、UP主昵称(模糊)、uid(模糊)筛选，支持自定义排序")
    public R<IPage<VideoPageDto>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String videoId,
            @RequestParam(required = false) Integer videoStatus,
            @RequestParam(required = false) String nickName,
            @RequestParam(required = false) String uid,
            @RequestParam(required = false) Integer field,
            @RequestParam(required = false) String descOrAsc,
            @RequestParam(required = false) Boolean isOngoing) {
        return R.success(videoService.pageVideo(page, size, title, categoryId, videoStatus, nickName, uid, field, descOrAsc, isOngoing,videoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询视频详情", description = "查询视频详情（含剧集列表、标签列表）")
    public R<VideoVo> getDetail(@PathVariable Long id) {
        return R.success(videoService.getDetailById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增视频", description = "新增视频（含剧集和标签）")
    public R<Video> add(@RequestBody VideoSaveDto dto) {
        return R.success(videoService.add(dto));
    }

    @PostMapping("/update")
    @Operation(summary = "编辑视频", description = "编辑视频（含剧集和标签）")
    public R<String> update(@RequestBody VideoSaveDto dto) {
        videoService.update(dto);
        return R.success("编辑视频成功");
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除视频", description = "删除视频（级联删除剧集和标签）")
    public R<String> delete(@PathVariable Long id) {
        videoService.delete(id);
        return R.success("删除视频成功");
    }

    @PostMapping("/review")
    @Operation(summary = "审核剧集", description = "审核单个剧集：0-待审核，1-审核通过，2-审核拒绝")
    public R<String> review(@RequestBody VideoEpisode episode) {
        videoService.updateReviewStatus(episode.getId(), episode.getReviewStatus());
        return R.success("审核操作成功");
    }

    @GetMapping("/episode/delete/{id}")
    @Operation(summary = "删除剧集", description = "删除单个剧集，同时更新视频的总集数和总时长")
    public R<String> deleteEpisode(@PathVariable Long id) {
        videoService.deleteEpisode(id);
        return R.success("删除剧集成功");
    }

    @GetMapping("/review-stats")
    @Operation(summary = "审核状态统计", description = "按视频级聚合审核状态分组计数：pendingCount-待审核, partialCount-部分审核, completedCount-审核完成")
    public R<VideoReviewStatsVo> reviewStats() {
        return R.success(videoService.getReviewStats());
    }

    @GetMapping("/category-stats")
    @Operation(summary = "分类统计", description = "按分类分组统计视频数量，categoryId 替换为分类中文名（优先Redis缓存）")
    public R<List<CategoryStatsVo>> categoryStats() {
        return R.success(videoService.getCategoryStats());
    }

    @PostMapping("/episode/save")
    @Operation(summary = "批量保存剧集", description = "批量保存剧集（增量更新：区分新增/更新/删除，保留统计数据和审核状态）")
    public R<String> saveEpisodes(@RequestBody VideoEpisodeSaveBatchDto dto) {
        return videoService.saveEpisodes(dto);
    }

    @GetMapping("/episode/avid/{avid}")
    @Operation(summary = "根据avid跳转指定集数", description = "通过剧集avid查找对应剧集及其父视频信息，实现指定集数跳转")
    public R<VideoEpisodeVo> getEpisodeByAvid(@PathVariable String avid) {
        return R.success(videoService.getEpisodeByAvid(avid));
    }


    /**
     * 根据视频avid和剧集avid跳转到指定集数播放页
     *
     * @param videoUid   视频avid（如 av1a2b3C）
     * @param episodeUid 剧集avid（如 avXyz789），用于前端标记当前播放的集数
     */
    @GetMapping("/{videoUid}/episode/{episodeUid}")
    @Operation(summary = "跳转到指定集数播放", description = "根据视频avid和剧集avid获取视频详情+全部剧集列表，前端根据episodeUid定位播放集数")
    public R<VideoVo> playEpisode(
            @Parameter(description = "视频avid") @PathVariable String videoUid,
            @Parameter(description = "剧集avid") @PathVariable String episodeUid) {
        VideoVo vo = videoService.getDetailByAvid(videoUid);
        // episodeUid 由前端从 episodes 列表中匹配 avid 来定位当前播放的集数
        // 服务端只需返回完整视频信息（含剧集列表），不需额外处理
        return R.success(vo);
    }
}
