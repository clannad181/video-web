package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.UpUserVo;

import java.io.IOException;

/**
 * 聚合搜索适配器接口（目标接口）：ES 与数据库两种搜索实现的统一入口。
 * ES 实现受 elasticsearch.enabled 条件装配，数据库实现始终注册；
 * 由 SearchServiceFacade 按开关选择具体实现，ES 异常时自动降级数据库查询
 */
public interface SearchAdapter {

    /**
     * UP主搜索：昵称或uid模糊匹配（keyword不传返回全部），按排序白名单倒序
     */
    IPage<UpUserVo> searchUp(String keyword, Integer page, Integer size, String sortType) throws IOException;

    /**
     * 视频搜索：标题模糊匹配（keyword不传返回全部），按排序白名单倒序
     */
    IPage<ClientVideoVo> searchVideo(String keyword, Integer page, Integer size, String sortType) throws IOException;

    /**
     * 前台视频列表浏览：全部可展示视频（可选按分类筛选），按 latest（发布时间）/hot（播放量）倒序
     */
    IPage<ClientVideoVo> listVideo(Integer page, Integer size, String sortType, Long categoryId) throws IOException;

    /**
     * 指定UP主投稿列表：仅该用户的已发布视频，按 latest（发布时间）/hot（播放量）倒序
     */
    IPage<ClientVideoVo> listUserVideos(Long userId, Integer page, Integer size, String sortType) throws IOException;
}
