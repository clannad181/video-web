package com.cjym.videoweb.controller.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.EpisodePageDto;
import com.cjym.videoweb.model.dto.EpisodeUpdateDto;
import com.cjym.videoweb.model.dto.VideoContributeDto;
import com.cjym.videoweb.model.dto.VideoEpisodeAddDto;
import com.cjym.videoweb.model.dto.VideoOngoingToggleDto;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.MonthlyContributionVo;
import com.cjym.videoweb.model.vo.VideoEpisodeVo;
import com.cjym.videoweb.model.vo.VideoVo;
import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @Operation(summary = "最新最火热视频分页查询", description = "分页查询前台展示的视频（仅已发布且至少有一集审核通过），sortType 可选 latest（最新，按发布时间倒序，默认）与 hot（最火热，按播放量倒序），categoryId 可选按分类筛选")
    public R<IPage<ClientVideoVo>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortType,
            @RequestParam(required = false) Long categoryId) {
        return R.success(videoService.pageClientVideo(page, size, sortType, categoryId));
    }

    /**
     * 分页查询指定用户（UP主）的所有投稿
     *
     * @param userUid  用户uid
     * @param page     页码，从1开始（默认1）
     * @param size     每页数量（默认10）
     * @param sortType 排序方式：latest（按发布时间倒序，默认）/hot（按播放量倒序）
     */
    @GetMapping("/user-videos")
    @Operation(summary = "指定用户的所有投稿", description = "分页查询指定用户（UP主）的投稿视频（仅已发布且至少有一集审核通过），sortType 可选 latest（按发布时间倒序，默认）/hot（按播放量倒序），游客可访问，含播放跳转链接与关注标记，用户不存在时报错")
    public R<IPage<ClientVideoVo>> userVideos(
            @Parameter(description = "用户uid") @RequestParam String userUid,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序方式（latest/hot）") @RequestParam(required = false) String sortType) {
        return R.success(videoService.pageUserVideos(userUid, page, size, sortType));
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
    @Operation(summary = "我的投稿", description = "双模式：startDate与endDate均传时按时间范围查询并按月份分组（yyyy-MM倒序，组内按投稿时间倒序）返回；均不传时改为普通分页查询（page/size默认1/10，videoStatus可选按状态筛选0-草稿1-待发布2-已发布3-已下架，title可选标题模糊搜索），按投稿时间倒序；每条记录含播放跳转链接playLink（优先最新审核通过剧集，无则回退到最新剧集供作者预览）")
    public R<Object> mine(
            @Parameter(description = "开始日期（含），格式 yyyy-MM-dd，需与endDate同时传") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期（含），格式 yyyy-MM-dd，需与startDate同时传") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "页码（分页模式，默认1）") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页条数（分页模式，默认10）") @RequestParam(required = false) Integer size,
            @Parameter(description = "视频状态筛选（分页模式）：0-草稿，1-待发布，2-已发布，3-已下架") @RequestParam(required = false) Integer videoStatus,
            @Parameter(description = "标题模糊搜索（分页模式）") @RequestParam(required = false) String title) {
        // 日期参数需成对传入：均传走按月分组模式，均不传走分页模式
        if (startDate != null && endDate != null) {
            return R.success(videoService.listMyContributions(startDate, endDate));
        }
        if (startDate != null || endDate != null) {
            throw new GlobeException("开始时间和结束时间需同时传入");
        }
        return R.success(videoService.pageMyContributions(page, size, videoStatus, title));
    }

    @PostMapping("/contribute")
    @Operation(summary = "投稿视频", description = "用户投稿视频（含剧集和标签），身份从token解析；审核流程跟随免审配置：免审关闭时自动发布，开启时进入待发布待审核")
    public R<String> contribute(@RequestBody VideoContributeDto dto) {
        videoService.contribute(dto);
        return R.success("投稿成功");
    }

    @PostMapping("/contribute/update")
    @Operation(summary = "编辑投稿", description = "按视频avid编辑自己的投稿（含剧集和标签），仅未发布前可编辑，审核流程跟随免审配置")
    public R<String> updateContribution(@RequestBody VideoContributeDto dto) {
        videoService.updateContribution(dto);
        return R.success("编辑成功");
    }

    @GetMapping("/contribute/delete")
    @Operation(summary = "删除投稿", description = "按视频avid删除自己的投稿（级联删除剧集）")
    public R<String> deleteContribution(
            @Parameter(description = "视频avid") @RequestParam String videoUid) {
        videoService.deleteContribution(videoUid);
        return R.success("删除成功");
    }

    @GetMapping("/contribute/detail")
    @Operation(summary = "投稿详情", description = "按视频avid查询自己的投稿详情（含剧集和标签），供编辑回显")
    public R<VideoVo> contributeDetail(
            @Parameter(description = "视频avid") @RequestParam String videoUid) {
        return R.success(videoService.getContributionDetail(videoUid));
    }

    @PostMapping("/contribute/ongoing/toggle")
    @Operation(summary = "切换连载状态", description = "按视频avid快速切换自己投稿的连载状态（自动取反，连载中→已完结、已完结→连载中），仅上传者可操作，返回切换后的连载状态")
    public R<Boolean> toggleOngoing(@RequestBody VideoOngoingToggleDto dto) {
        return R.success(videoService.toggleOngoing(dto.getVideoUid()));
    }

    @PostMapping("/contribute/episode/add")
    @Operation(summary = "添加剧集", description = "给连载中的自己投稿添加新剧集（按视频avid定位，集数序号与排序不传时自动追加到末尾），仅上传者可操作，审核流程跟随免审配置")
    public R<VideoEpisode> addEpisode(@RequestBody VideoEpisodeAddDto dto) {
        return R.success(videoService.addEpisode(dto));
    }

    @PostMapping("/contribute/episode/list")
    @Operation(summary = "剧集分页查询", description = "按视频avid分页查询自己投稿的剧集（仅上传者可查），多条件：title剧集标题模糊搜索、reviewStatus审核状态筛选（0-待审核，1-审核通过，2-审核拒绝），按排序与集数序号降序，每条含播放跳转链接")
    public R<IPage<VideoEpisodeVo>> episodeList(@RequestBody EpisodePageDto dto) {
        return R.success(videoService.pageMyEpisodes(dto));
    }

    @PostMapping("/contribute/episode/update")
    @Operation(summary = "编辑剧集", description = "按剧集avid编辑自己投稿的剧集（仅上传者可操作，集数序号与排序不传时保持原值），标题或播放地址变更时重置审核状态为待审核，并重算视频总集数与总时长")
    public R<String> updateEpisode(@RequestBody EpisodeUpdateDto dto) {
        videoService.updateEpisode(dto);
        return R.success("编辑成功");
    }

    @GetMapping("/contribute/episode/delete")
    @Operation(summary = "删除剧集", description = "按剧集avid删除自己投稿的剧集（仅上传者可操作，仅连载中视频可删除），并重算视频总集数与总时长")
    public R<String> deleteEpisode(
            @Parameter(description = "剧集avid") @RequestParam String episodeUid) {
        videoService.deleteEpisodeByUid(episodeUid);
        return R.success("删除成功");
    }
}
