package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.dto.VideoSaveDto;
import com.cjym.videoweb.model.entity.Video;
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
    @Operation(summary = "分页查询视频列表", description = "支持按标题模糊搜索、分类、审核状态、UP主筛选")
    public R<IPage<VideoPageDto>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer reviewStatus,
            @RequestParam(required = false) Long userId) {
        return R.success(videoService.pageVideo(page, size, title, categoryId, reviewStatus, userId));
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
    @Operation(summary = "审核视频", description = "更新视频审核状态：0-待审核，1-审核通过，2-审核拒绝")
    public R<String> review(@RequestBody Video video) {
        videoService.updateReviewStatus(video.getId(), video.getReviewStatus());
        return R.success("审核操作成功");
    }
}
