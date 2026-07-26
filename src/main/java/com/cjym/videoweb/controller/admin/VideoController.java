package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.VideoEpisodeSaveBatchDto;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.dto.VideoSaveDto;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.vo.VideoReviewStatsVo;
import com.cjym.videoweb.model.vo.VideoVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) Integer reviewStatus,
            @RequestParam(required = false) String nickName,
            @RequestParam(required = false) String uid,
            @RequestParam(required = false) Integer field,
            @RequestParam(required = false) String descOrAsc,
            @RequestParam(required = false) Boolean isOngoing) {
        return R.success(videoService.pageVideo(page, size, title, categoryId, reviewStatus, nickName, uid, field, descOrAsc, isOngoing));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询视频详情", description = "查询视频详情（含剧集列表、标签列表）")
    public R<VideoVo> getDetail(@PathVariable Long id) {
        return R.success(videoService.getDetailById(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增视频", description = "新增视频（含剧集和标签）")
    public R<String> add(@RequestBody VideoSaveDto dto) {
        videoService.add(dto);
        return R.success("新增视频成功");
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

    @PostMapping("/episode/save")
    @Operation(summary = "批量保存剧集", description = "批量保存剧集（增量更新：区分新增/更新/删除，保留统计数据和审核状态）")
    public R<String> saveEpisodes(@RequestBody VideoEpisodeSaveBatchDto dto) {
        return videoService.saveEpisodes(dto);
    }
}
