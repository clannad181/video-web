package com.cjym.videoweb.model.es;

import lombok.Data;

/**
 * 聚合搜索视频索引文档（video_search 索引）
 */
@Data
public class VideoDoc {

    /** 视频id（作为ES文档id） */
    private Long id;

    /** 视频avid */
    private String avid;

    /** 视频标题（模糊搜索字段） */
    private String title;

    /** 封面图URL */
    private String coverUrl;

    /** 总时长（秒，多集累加） */
    private Long totalDuration;

    /** 总集数 */
    private Integer episodeCount;

    /** 播放数（多集累加） */
    private Long playCount;

    /** 收藏数（多集累加） */
    private Long favoriteCount;

    /** 最新更新剧集avid（审核通过剧集中集数序号最大的一集） */
    private String episodeAvid;

    /** 是否连载中 */
    private Boolean isOngoing;

    /** 发布时间（epoch毫秒，用于排序） */
    private Long createTime;

    /** UP主用户id */
    private Long userId;

    /** 分类名称 */
    private String categoryName;

    /** UP主昵称 */
    private String nickName;

    /** UP主头像URL */
    private String avatarUrl;

    /** UP主uid */
    private String uid;
}
