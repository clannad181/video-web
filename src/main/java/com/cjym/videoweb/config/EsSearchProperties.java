package com.cjym.videoweb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Elasticsearch 配置属性（application.yml 中 elasticsearch 前缀）
 */
@Data
@ConfigurationProperties(prefix = "elasticsearch")
public class EsSearchProperties {

    /**
     * 是否启用 Elasticsearch：true 时装配 ES 客户端，
     * 配合 web_config 中 search/es_search=true 时聚合搜索走 ES
     */
    private boolean enabled;

    /**
     * ES 服务地址
     */
    private String uris;
}
