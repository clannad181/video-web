package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评论详情（管理端）
 * <p>
 * 返回目标评论所属的完整评论树：根评论携带全量子回复树，
 * 树中每个节点均包含评论者人员信息（昵称、头像）与视频信息（标题、封面）。
 * 管理端可见全部状态（正常/用户删除/运营屏蔽/待审核）。
 */
@Data
@Schema(description = "评论详情（管理端）")
public class CommentDetailVo {

    @Schema(description = "目标评论ID（前端用于定位高亮）")
    private Long targetId;

    @Schema(description = "根评论（含全部子回复树，每个节点携带评论者昵称头像与视频标题封面）")
    private CommentVo rootComment;
}
