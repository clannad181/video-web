package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.VideoEpisodeSaveBatchDto;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.dto.VideoSaveDto;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.vo.CategoryStatsVo;
import com.cjym.videoweb.model.vo.ClientVideoDetailVo;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.VideoEpisodeVo;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.VideoReviewStatsVo;
import com.cjym.videoweb.model.vo.VideoVo;
import com.cjym.videoweb.result.R;

import java.time.LocalDate;
import java.util.List;

public interface VideoService extends IService<Video> {

    /**
     * 分页查询视频列表
     */
    IPage<VideoPageDto> pageVideo(Integer page, Integer size, String title,
                                   Long categoryId, Integer videoStates, String nickName, String uid,
                                   Integer field, String descOrAsc,Boolean isOngoing,String avid);

    /**
     * 分页查询前台展示视频（仅已发布且至少有一集审核通过），支持最新/最火热排序
     */
    IPage<ClientVideoVo> pageClientVideo(Integer page, Integer size, String sortType);

    /**
     * 前台视频推荐：已登录返回关注的UP主最新视频（不足时用最多收藏补齐），未登录返回最多收藏视频，最多6条
     */
    List<ClientVideoVo> recommend();

    /**
     * 相关视频推荐分页：根据视频avid查询同UP主或标签有交集的视频（排除自身，仅已发布且至少有一集审核通过）；
     * 标签为后台查询数据库得到的当前视频标签（逗号分割），前端无需传入
     */
    IPage<ClientVideoVo> pageRelatedVideos(Integer page, Integer size, String videoUid);

    /**
     * 前台视频播放详情：根据视频avid和剧集avid返回视频信息+审核通过剧集列表+当前剧集+UP主信息（仅限已发布视频）
     */
    ClientVideoDetailVo getClientPlayDetail(String videoUid, String episodeUid);

    /**
     * 查询视频详情（含剧集和标签）
     */
    VideoVo getDetailById(Long id);

    /**
     * 根据 avid 查询视频详情（含剧集和标签）
     */
    VideoVo getDetailByAvid(String avid);

    /**
     * 新增视频
     */
    Video add(VideoSaveDto dto);

    /**
     * 编辑视频
     */
    void update(VideoSaveDto dto);

    /**
     * 删除视频（级联删除剧集和标签）
     */
    void delete(Long id);

    /**
     * 批量保存剧集（增量更新：区分新增/更新/删除）
     *
     * @return
     */
    R<String> saveEpisodes(VideoEpisodeSaveBatchDto dto);

    /**
     * 审核剧集
     */
    void updateReviewStatus(Long episodeId, Integer reviewStatus);
    /**
     * 删除剧集
     */
    void deleteEpisode(Long episodeId);

    /**
     * 查询视频审核状态统计（按视频级聚合审核状态分组计数）
     */
    VideoReviewStatsVo getReviewStats();

    /**
     * 查询视频分类统计（按分类分组计数，categoryId 替换为分类中文名）
     */
    List<CategoryStatsVo> getCategoryStats();

    /**
     * 根据剧集 avid 跳转指定集数（返回剧集+视频基本信息）
     */
    VideoEpisodeVo getEpisodeByAvid(String avid);

    /**
     * 分页查询待审核视频（video_status=1 且 review_status=0 或 1），支持按标题和UP主昵称筛选
     */
    IPage<VideoPageDto> pageReview(Integer page, Integer size, String title, String nickName);

    /**
     * 视频每日增量：按日期范围统计每天新增视频数（无数据的日期补0）
     */
    List<DailyIncrementVo> getDailyIncrement(LocalDate startDate, LocalDate endDate);
}
