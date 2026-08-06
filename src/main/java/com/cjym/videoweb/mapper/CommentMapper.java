package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.entity.Comment;
import com.cjym.videoweb.model.vo.CommentStatusStatsVo;
import com.cjym.videoweb.model.vo.CommentVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 分页查询视频一级评论（JOIN user_info，支持按热度/时间排序）
     */
    IPage<CommentVo> selectRootComments(Page<CommentVo> page,
                                         @Param("videoId") Long videoId,
                                         @Param("sortBy") String sortBy,
                                         @Param("currentUserId") Long currentUserId);

    /**
     * 查询某根评论的所有子回复（按时间正序，用于懒加载展开）
     */
    List<CommentVo> selectReplies(@Param("rootId") Long rootId,
                                   @Param("currentUserId") Long currentUserId);

    /**
     * 管理端多条件分页查询评论列表（含回复，支持按内容关键词、昵称、状态、视频ID、时间范围筛选）
     */
    IPage<CommentVo> selectAdminList(Page<CommentVo> page,
                                      @Param("videoId") Long videoId,
                                      @Param("status") Integer status,
                                      @Param("keyword") String keyword,
                                      @Param("nickName") String nickName,
                                      @Param("startTime") String startTime,
                                      @Param("endTime") String endTime);

    /**
     * 评论状态统计：按 status 分组计数
     */
    CommentStatusStatsVo selectStatusStats();
}
