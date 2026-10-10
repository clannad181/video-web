package com.cjym.videoweb.model.vo;

import com.cjym.videoweb.model.dto.UserPageDto;
import com.cjym.videoweb.model.dto.VideoPageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理端全局搜索结果：一次返回视频、用户、评论三类匹配结果
 */
@Data
@Schema(description = "管理端全局搜索结果")
public class GlobalSearchVo {

    @Schema(description = "匹配的视频列表（标题或avid模糊匹配）")
    private List<VideoPageDto> videos;

    @Schema(description = "匹配的用户列表（昵称、用户名、邮箱或uid模糊匹配）")
    private List<UserPageDto> users;

    @Schema(description = "匹配的评论列表（内容模糊匹配，含评论者昵称与视频标题）")
    private List<CommentVo> comments;
}
