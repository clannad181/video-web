package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.CommentSaveDto;
import com.cjym.videoweb.model.entity.Comment;
import com.cjym.videoweb.model.entity.CommentLike;
import com.cjym.videoweb.model.vo.DailyIncrementVo;
import com.cjym.videoweb.model.vo.CommentStatusStatsVo;
import com.cjym.videoweb.model.vo.CommentVo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CommentService extends IService<Comment> {

    /**
     * 发表评论/回复
     */
    CommentVo create(CommentSaveDto dto);

    /**
     * 用户删除自己的评论（软删 status=1）
     */
    void deleteByUser(Long commentId);

    /**
     * 分页获取视频一级评论（episodeId 传则查剧集评论，不传则查视频级历史评论）
     */
    IPage<CommentVo> pageRootComments(Integer page, Integer size, Long videoId, Long episodeId, String sortBy);

    /**
     * 根据视频avid和剧集avid分页获取评论树（一级评论内嵌树形子回复）
     */
    IPage<CommentVo> pageCommentTree(Integer page, Integer size, String videoUid, String episodeUid, String sortBy);

    /**
     * 获取某根评论的所有子回复列表
     */
    List<CommentVo> getReplies(Long rootId);

    /**
     * 批量统计剧集评论总数（前台可见口径：status != 2），返回剧集ID → 评论数映射
     */
    Map<Long, Long> getEpisodeCommentCounts(List<Long> episodeIds);

    /**
     * 点赞/取消点赞（toggle）
     */
    CommentLike toggleLike(Long commentId);

    /**
     * 运营屏蔽评论（软删 status=2）
     */
    void deleteByAdmin(Long commentId);

    /**
     * 管理端多条件分页查询评论列表
     */
    IPage<CommentVo> pageAdminList(Integer page, Integer size,
                                    Long videoId, Integer status,
                                    String keyword, String nickName,
                                    String startTime, String endTime);

    /**
     * 评论状态统计：按 status 分组计数
     */
    CommentStatusStatsVo getStatusStats();

    /**
     * 分页查询待审核评论（status=3），支持按评论内容、用户昵称、视频标题筛选
     */
    IPage<CommentVo> pageReviewList(Integer page, Integer size,
                                     String content, String nickName, String videoTitle);

    /**
     * 审核评论：通过 → status=0，拒绝 → status=2
     */
    void review(Long commentId, Integer reviewStatus);

    /**
     * 评论每日增量：按日期范围统计每天新增评论数（无数据的日期补0）
     */
    List<DailyIncrementVo> getDailyIncrement(LocalDate startDate, LocalDate endDate);
}
