package com.cjym.videoweb.model.es;

import lombok.Data;

/**
 * 聚合搜索UP主索引文档（up_search 索引）
 */
@Data
public class UpUserDoc {

    /** 用户id（作为ES文档id） */
    private Long id;

    /** 用户唯一标识uid（16位随机数字，模糊搜索字段） */
    private String uid;

    /** 用户昵称（模糊搜索字段） */
    private String nikeName;

    /** 用户头像URL */
    private String avatar;

    /** 用户个性签名 */
    private String descript;

    /** 性别 */
    private Boolean sex;

    /** 是否为大会员 */
    private Boolean isVip;

    /** 关注数 */
    private Long followCount;

    /** 粉丝数 */
    private Long fansCount;

    /** 累计获赞数（所有剧集点赞数之和） */
    private Long totalLikeCount;

    /** 累计播放数（所有剧集播放数之和） */
    private Long totalPlayCount;
}
