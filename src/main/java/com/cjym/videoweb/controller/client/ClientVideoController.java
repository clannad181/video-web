package com.cjym.videoweb.controller.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.MonthlyContributionVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 视频浏览（客户端）
 */
@RestController
@RequestMapping("/client/video")
@Tag(name = "视频浏览", description = "前台视频列表浏览")
public class ClientVideoController {

    @Resource
    private VideoService videoService;

    @GetMapping("/list")
    @Operation(summary = "最新最火热视频分页查询", description = "分页查询前台展示的视频（仅已发布且至少有一集审核通过），sortType 可选 latest（最新，按发布时间倒序，默认）与 hot（最火热，按播放量倒序）")
    public R<IPage<ClientVideoVo>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortType) {
        return R.success(videoService.pageClientVideo(page, size, sortType));
    }

    @GetMapping("/recommend")
    @Operation(summary = "视频推荐", description = "已登录：返回所有关注UP主的最新视频6条，关注视频不足6条（含关注数为0）时用最多收藏的视频补齐；未登录：返回最多收藏的视频")
    public R<List<ClientVideoVo>> recommend() {
        return R.success(videoService.recommend());
    }

    @GetMapping("/related")
    @Operation(summary = "相关视频推荐", description = "根据视频avid推荐相关视频：同UP主优先、标签有交集的视频补充（标签为后台查询数据库得到的当前视频标签，逗号分割），排除自身，仅已发布且至少有一集审核通过，再按播放量降序")
    public R<IPage<ClientVideoVo>> related(
            @Parameter(description = "当前视频avid") @RequestParam String videoUid,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.success(videoService.pageRelatedVideos(page, size, videoUid));
    }

    @GetMapping("/mine")
    @Operation(summary = "我的投稿", description = "获取当前登录用户指定时间范围内的投稿视频（含全部状态），按月份分组（yyyy-MM，倒序），组内按投稿时间倒序")
    public R<List<MonthlyContributionVo>> mine(
            @Parameter(description = "开始日期（含），格式 yyyy-MM-dd") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期（含），格式 yyyy-MM-dd") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return R.success(videoService.listMyContributions(startDate, endDate));
    }
}
