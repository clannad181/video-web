package com.cjym.videoweb.controller.client;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.vo.CommentVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台视频评论（树结构）
 */
@RestController
@RequestMapping("/client/video")
@Tag(name = "视频评论（前台）", description = "前台视频剧集评论树查询，路径与 /admin/comment 完全隔离")
public class ClientCommentController {

    @Resource
    private CommentService commentService;

    /**
     * 获取对应视频对应剧集的评论（树结构）
     *
     * @param videoUid   视频avid（如 av1a2b3C）
     * @param episodeUid 剧集avid（如 avXyz789）
     */
    @GetMapping("/{videoUid}/episode/{episodeUid}/comments")
    @Operation(summary = "获取剧集评论树", description = "根据视频avid和剧集avid分页查询一级评论，每条一级评论内嵌树形子回复；sortBy: like-按热度排序, time-按时间排序（默认）")
    public R<IPage<CommentVo>> comments(
            @Parameter(description = "视频avid") @PathVariable String videoUid,
            @Parameter(description = "剧集avid") @PathVariable String episodeUid,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "time") String sortBy) {
        return R.success(commentService.pageCommentTree(page, size, videoUid, episodeUid, sortBy));
    }
}
