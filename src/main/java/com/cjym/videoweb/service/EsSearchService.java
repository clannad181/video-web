package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.UpUserVo;

import java.io.IOException;

/**
 * Elasticsearch 聚合搜索适配器（仅 elasticsearch.enabled=true 时注册实现类）：
 * 搜索能力继承 {@link SearchAdapter}，同时承担 ES 索引同步职责（业务埋点增量同步 + 启动全量重建）
 */
public interface EsSearchService extends SearchAdapter {

    /**
     * ES UP主搜索：昵称或uid模糊匹配（keyword不传返回全部），按排序白名单倒序
     */
    @Override
    IPage<UpUserVo> searchUp(String keyword, Integer page, Integer size, String sortType) throws IOException;

    /**
     * ES 视频搜索：标题模糊匹配（keyword不传返回全部），按排序白名单倒序
     */
    @Override
    IPage<ClientVideoVo> searchVideo(String keyword, Integer page, Integer size, String sortType) throws IOException;

    /**
     * ES 前台视频列表浏览：全部可展示视频（可选按分类筛选），按 latest/hot 倒序
     */
    @Override
    IPage<ClientVideoVo> listVideo(Integer page, Integer size, String sortType, Long categoryId) throws IOException;

    /**
     * ES 指定UP主投稿列表：仅该用户的已发布视频，按 latest/hot 倒序
     */
    @Override
    IPage<ClientVideoVo> listUserVideos(Long userId, Integer page, Integer size, String sortType) throws IOException;

    /**
     * 视频变更后同步索引：满足"已发布且至少有一集审核通过"则更新文档，否则删除文档
     */
    void refreshVideo(Long videoId);

    /**
     * 删除视频索引文档
     */
    void deleteVideo(Long videoId);

    /**
     * 用户变更后同步索引：用户存在则更新文档，已删除则移除文档
     */
    void refreshUser(Long userId);

    /**
     * 全量重建：重建两个索引并同步所有可搜索的视频与用户
     */
    void rebuildAll() throws IOException;
}
