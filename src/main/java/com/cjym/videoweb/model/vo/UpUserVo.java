package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * UP主公开信息（收藏搜索等场景对外展示）
 */
@Data
@Schema(description = "UP主公开信息")
public class UpUserVo {

    @Schema(description = "用户唯一标识uid（16位随机数字）")
    private String uid;

    @Schema(description = "用户昵称")
    private String nikeName;

    @Schema(description = "用户头像URL")
    private String avatar;

    @Schema(description = "用户个性签名")
    private String descript;

    @Schema(description = "性别")
    private Boolean sex;

    @Schema(description = "是否为大会员")
    private Boolean isVip;

    @Schema(description = "关注数")
    private Long followCount;

    @Schema(description = "粉丝数")
    private Long fansCount;

    @Schema(description = "累计获赞数（所有剧集点赞数之和）")
    private Long totalLikeCount;

    @Schema(description = "累计播放数（所有剧集播放数之和）")
    private Long totalPlayCount;
}
