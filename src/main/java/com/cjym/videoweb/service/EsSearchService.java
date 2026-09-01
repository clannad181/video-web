package com.cjym.videoweb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.UpUserVo;

import java.io.IOException;

/**
 * Elasticsearch 聚合搜索服务（仅 elasticsearch.enabled=true 时注册实现类）
 */
public interface EsSearchService {

    /**
     * ES UP主搜索：昵称或uid模糊匹配（keyword不传返回全部），按排序白名单倒序
     */
    IPage<UpUserVo> searchUp(String keyword, Integer page, Integer size, String sortType) throws IOException;

    /**
     * ES 视频搜索：标题模糊匹配（keyword不传返回全部），按排序白名单倒序
     */
    IPage<ClientVideoVo> searchVideo(String keyword, Integer page, Integer size, String sortType) throws IOException;

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
