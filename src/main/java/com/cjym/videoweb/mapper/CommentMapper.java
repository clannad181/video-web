package com.cjym.videoweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cjym.videoweb.model.entity.Comment;
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
}
