-- ============================================
-- Elasticsearch 聚合搜索开关配置（存放在 web_config 表）
-- ============================================

-- 聚合搜索是否走 Elasticsearch（true-开启ES搜索，false-回退数据库查询）
-- 前提：application.yml 中 elasticsearch.enabled=true 装配了 ES 客户端
INSERT INTO public.web_config (category, key, value, description)
VALUES ('search', 'es_search', 'false', '聚合搜索是否走 Elasticsearch（true-开启ES搜索，false-回退数据库查询）');
