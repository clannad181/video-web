package com.cjym.videoweb.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.dto.CommentSaveDto;
import com.cjym.videoweb.model.vo.CommentStatusStatsVo;
import com.cjym.videoweb.model.vo.CommentVo;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/comment")
@Tag(name = "评论管理", description = "视频评论管理")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping
    @Operation(summary = "发表评论/回复", description = "发表一级评论或回复子评论：parentId不传或传0为一级评论，传父评论ID为回复")
    public R<CommentVo> create(@RequestBody CommentSaveDto dto) {
        return R.success(commentService.create(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "管理端多条件分页查询评论列表", description = "支持按视频ID、状态、内容关键词、用户昵称、时间范围筛选")
    public R<IPage<CommentVo>> adminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long videoId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String nickName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return R.success(commentService.pageAdminList(page, size, videoId, status, keyword, nickName, startTime, endTime));
    }

    @GetMapping
    @Operation(summary = "分页获取视频一级评论", description = "sortBy: like-按热度(点赞数)排序, time-按时间排序（默认）")
    public R<IPage<CommentVo>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long videoId,
            @RequestParam(required = false, defaultValue = "time") String sortBy) {
        return R.success(commentService.pageRootComments(page, size, videoId, sortBy));
    }

    @GetMapping("/replies/{rootId}")
    @Operation(summary = "获取子回复列表", description = "获取某根评论下的所有子回复（平铺，按时间正序）")
    public R<List<CommentVo>> replies(@PathVariable Long rootId) {
        return R.success(commentService.getReplies(rootId));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "用户删除评论", description = "用户删除自己的评论（软删，仅标记状态为1）")
    public R<String> deleteByUser(@PathVariable Long commentId) {
        commentService.deleteByUser(commentId);
        return R.success("删除评论成功");
    }

    @PostMapping("/{commentId}/like")
    @Operation(summary = "点赞/取消点赞", description = "toggle逻辑：已点赞则取消，未点赞则点赞；返回最新点赞状态")
    public R<String> like(@PathVariable Long commentId) {
        var result = commentService.toggleLike(commentId);
        return R.success(result.getStatus() == 1 ? "点赞成功" : "已取消点赞");
    }

    @GetMapping("/admin-delete/{commentId}")
    @Operation(summary = "运营屏蔽或恢复评论", description = "管理员屏蔽违规评论（软删，标记状态为2）")
    public R<String> deleteByAdmin(@PathVariable Long commentId) {
        commentService.deleteByAdmin(commentId);
        return R.success("屏蔽评论成功");
    }

    @GetMapping("/status-stats")
    @Operation(summary = "评论状态统计", description = "统计各状态评论数量：正常、用户删除、运营屏蔽及总数")
    public R<CommentStatusStatsVo> statusStats() {
        return R.success(commentService.getStatusStats());
    }
}
