package com.cjym.videoweb.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cjym.videoweb.constant.BaseConstant;
import com.cjym.videoweb.model.vo.ClientVideoVo;
import com.cjym.videoweb.model.vo.UpUserVo;
import com.cjym.videoweb.service.EsSearchService;
import com.cjym.videoweb.service.SearchAdapter;
import com.cjym.videoweb.service.WebConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * 聚合搜索门面（路由）：按开关在 ES 与数据库两种适配器间选择，
 * ES 搜索异常时自动降级数据库查询，保证聚合搜索接口稳定可用
 */
@Slf4j
@Service
public class SearchServiceFacade implements SearchAdapter {

    @Resource
    private DatabaseSearchAdapter databaseSearchAdapter;

    /**
     * ES 搜索适配器：仅 elasticsearch.enabled=true 时存在，未启用时为 null（搜索走数据库）
     */
    @Resource
    private ObjectProvider<EsSearchService> esSearchServiceProvider;

    @Resource
    private WebConfigService webConfigService;

    @Override
    public IPage<UpUserVo> searchUp(String keyword, Integer page, Integer size, String sortType) {
        // ES 已装配且 web_config 开关开启：走 Elasticsearch（粉丝数/关注数由事件机制同步维护），异常自动降级数据库查询
        EsSearchService esSearchService = esSearchServiceProvider.getIfAvailable();
        if (esSearchService != null && isEsSearchEnabled()) {
            try {
                return esSearchService.searchUp(keyword, page, size, sortType);
            } catch (Exception e) {
                log.warn("ES UP主搜索失败，降级为数据库查询: {}", e.getMessage());
            }
        }
        return databaseSearchAdapter.searchUp(keyword, page, size, sortType);
    }

    @Override
    public IPage<ClientVideoVo> searchVideo(String keyword, Integer page, Integer size, String sortType) {
        // ES 已装配且 web_config 开关开启：走 Elasticsearch，异常自动降级数据库查询
        EsSearchService esSearchService = esSearchServiceProvider.getIfAvailable();
        if (esSearchService != null && isEsSearchEnabled()) {
            try {
                return esSearchService.searchVideo(keyword, page, size, sortType);
            } catch (Exception e) {
                log.warn("ES 视频搜索失败，降级为数据库查询: {}", e.getMessage());
            }
        }
        return databaseSearchAdapter.searchVideo(keyword, page, size, sortType);
    }

    @Override
    public IPage<ClientVideoVo> listVideo(Integer page, Integer size, String sortType, Long categoryId) {
        // ES 已装配且 web_config 开关开启：走 Elasticsearch，异常自动降级数据库查询
        EsSearchService esSearchService = esSearchServiceProvider.getIfAvailable();
        if (esSearchService != null && isEsSearchEnabled()) {
            try {
                return esSearchService.listVideo(page, size, sortType, categoryId);
            } catch (Exception e) {
                log.warn("ES 视频列表查询失败，降级为数据库查询: {}", e.getMessage());
            }
        }
        return databaseSearchAdapter.listVideo(page, size, sortType, categoryId);
    }

    @Override
    public IPage<ClientVideoVo> listUserVideos(Long userId, Integer page, Integer size, String sortType) {
        // ES 已装配且 web_config 开关开启：走 Elasticsearch，异常自动降级数据库查询
        EsSearchService esSearchService = esSearchServiceProvider.getIfAvailable();
        if (esSearchService != null && isEsSearchEnabled()) {
            try {
                return esSearchService.listUserVideos(userId, page, size, sortType);
            } catch (Exception e) {
                log.warn("ES UP主投稿列表查询失败，降级为数据库查询: {}", e.getMessage());
            }
        }
        return databaseSearchAdapter.listUserVideos(userId, page, size, sortType);
    }

    /**
     * 聚合搜索是否走 Elasticsearch（web_config 中 search/es_search 配置，Redis缓存优先）
     */
    private boolean isEsSearchEnabled() {
        String value = webConfigService.getConfigValue(
                BaseConstant.WEB_CONFIG_CATEGORY_SEARCH, BaseConstant.WEB_CONFIG_KEY_ES_SEARCH);
        return BaseConstant.WEB_CONFIG_VALUE_TRUE.equalsIgnoreCase(value);
    }
}
