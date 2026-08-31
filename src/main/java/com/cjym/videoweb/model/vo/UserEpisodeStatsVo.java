package com.cjym.videoweb.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户剧集累计统计")
public class UserEpisodeStatsVo {

    @Schema(description = "累计获赞数（所有剧集点赞数之和）")
    private Long totalLikeCount;

    @Schema(description = "累计播放数（所有剧集播放数之和）")
    private Long totalPlayCount;
}
