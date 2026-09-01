package com.cjym.videoweb.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 客户端配置：仅在 elasticsearch.enabled=true 时装配，
 * 关闭时不创建任何 ES 连接，项目运行行为与未接入 ES 时完全一致
 */
@Configuration
@EnableConfigurationProperties(EsSearchProperties.class)
@ConditionalOnProperty(prefix = "elasticsearch", name = "enabled", havingValue = "true")
public class EsSearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient(EsSearchProperties props) {
        RestClient restClient = RestClient.builder(HttpHost.create(props.getUris())).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
