package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cjym.videoweb.model.dto.CommentSaveDto;
import com.cjym.videoweb.model.entity.Comment;
import com.cjym.videoweb.model.entity.CommentLike;
import com.cjym.videoweb.model.vo.CommentVo;

import java.util.List;

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
     * 分页获取视频一级评论
     */
    IPage<CommentVo> pageRootComments(Integer page, Integer size, Long videoId, String sortBy);

    /**
     * 获取某根评论的所有子回复列表
     */
    List<CommentVo> getReplies(Long rootId);

    /**
     * 点赞/取消点赞（toggle）
     */
    CommentLike toggleLike(Long commentId);

    /**
     * 运营屏蔽评论（软删 status=2）
     */
    void deleteByAdmin(Long commentId);
}
