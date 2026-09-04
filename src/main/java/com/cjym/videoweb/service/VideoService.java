package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.EpisodePageDto;
import com.cjym.videoweb.model.dto.EpisodeUpdateDto;
import com.cjym.videoweb.model.dto.VideoContributeDto;
import com.cjym.videoweb.model.dto.VideoEpisodeAddDto;
import com.cjym.videoweb.model.dto.VideoEpisodeSaveBatchDto;
import com.cjym.videoweb.model.dto.VideoPageDto;
import com.cjym.videoweb.model.dto.VideoSaveDto;
import com.cjym.videoweb.model.entity.Video;
import com.cjym.videoweb.model.entity.VideoEpisode;
import com.cjym.videoweb.model.vo.CategoryStatsVo;
import com.cjym.videoweb.model.vo.ClientVideoDetailVo;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.DataCenterVo;
import com.cjym.videoweb.model.vo.MonthlyContributionVo;
import com.cjym.videoweb.model.vo.MyContributionVideoVo;
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
     * 分页查询前台展示视频（仅已发布且至少有一集审核通过），支持最新/最火热排序与分类筛选
     */
    IPage<ClientVideoVo> pageClientVideo(Integer page, Integer size, String sortType, Long categoryId);

    /**
     * 指定UP主投稿分页：按userUid定位UP主，返回其已发布且至少一集审核通过的投稿（游客可访问），
     * 按发布时间（latest）/播放量（hot）倒序，含播放跳转链接与关注标记
     */
    IPage<ClientVideoVo> pageUserVideos(String userUid, Integer page, Integer size, String sortType);

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

    /**
     * 我的投稿：查询当前登录用户指定时间范围内的投稿视频，按月份分组（月份倒序，组内按投稿时间倒序）
     */
    List<MonthlyContributionVo> listMyContributions(LocalDate startDate, LocalDate endDate);

    /**
     * 我的投稿分页：分页查询当前登录用户的投稿（全部状态，可选按状态与标题筛选），按投稿时间倒序
     */
    IPage<MyContributionVideoVo> pageMyContributions(Integer page, Integer size, Integer videoStatus, String title);

    /**
     * 用户端投稿视频（身份从token解析，审核流程跟随免审配置：免审关闭时自动发布，开启时进入待发布待审核）
     */
    Video contribute(VideoContributeDto dto);

    /**
     * 用户端编辑投稿（按avid定位自己的视频，仅未发布前可编辑，审核流程跟随免审配置）
     */
    void updateContribution(VideoContributeDto dto);

    /**
     * 用户端删除投稿（按avid定位自己的视频，级联删除剧集）
     */
    void deleteContribution(String videoUid);

    /**
     * 用户端投稿详情（按avid查询自己的视频，含剧集和标签，供编辑回显）
     */
    VideoVo getContributionDetail(String videoUid);

    /**
     * 用户端快速切换连载状态（按avid定位自己的视频，自动取反isOngoing，仅上传者可操作，返回切换后的连载状态）
     */
    Boolean toggleOngoing(String videoUid);

    /**
     * 用户端连载中视频添加剧集（按avid定位自己的视频，仅连载中可添加，集数序号与排序不传时自动追加到末尾，审核流程跟随免审配置）
     */
    VideoEpisode addEpisode(VideoEpisodeAddDto dto);

    /**
     * 用户端剧集分页查询（按视频avid定位自己的视频，支持标题模糊搜索与审核状态筛选，按排序与集数序号升序，含播放跳转链接）
     */
    IPage<VideoEpisodeVo> pageMyEpisodes(EpisodePageDto dto);

    /**
     * 用户端编辑剧集（按剧集avid定位自己的视频剧集，标题或播放地址变更时重置审核状态，级联重算视频统计）
     */
    void updateEpisode(EpisodeUpdateDto dto);

    /**
     * 用户端删除剧集（按剧集avid定位自己的视频剧集，仅连载中视频可删除，级联重算视频统计）
     */
    void deleteEpisodeByUid(String episodeUid);

    /**
     * 前台个人数据中心：自己所有投稿的观看/收藏/投币/点赞累计 + 最热视频TOP5（观看数最多）+ 最受欢迎视频TOP5（收藏数最多）
     */
    DataCenterVo getDataCenter();

    /**
     * 聚合搜索视频分页：标题模糊搜索（keyword 不传返回全部，仅已发布且至少一集审核通过），按排序白名单列倒序，含播放跳转链接与关注标记
     */
    IPage<ClientVideoVo> pageSearchVideo(String keyword, Integer page, Integer size, String sortType);
}
