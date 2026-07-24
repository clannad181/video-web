-- ============================================
-- Token 有效期配置（存放在 web_config 表）
-- 值单位：毫秒
-- ============================================

-- 前台 Access Token 有效期（默认 15 分钟 = 900000 ms）
INSERT INTO public.web_config (category, key, value, description)
VALUES ('token_expiry', 'front_access_token_expiry', '900000', '前台 Access Token 有效期（毫秒）');

-- 前台 Refresh Token 有效期（默认 7 天 = 604800000 ms）
INSERT INTO public.web_config (category, key, value, description)
VALUES ('token_expiry', 'front_refresh_token_expiry', '604800000', '前台 Refresh Token 有效期（毫秒）');

-- 后台 Access Token 有效期（默认 30 分钟 = 1800000 ms）
INSERT INTO public.web_config (category, key, value, description)
VALUES ('token_expiry', 'backend_access_token_expiry', '1800000', '后台 Access Token 有效期（毫秒）');

-- 后台 Refresh Token 有效期（默认 7 天 = 604800000 ms）
INSERT INTO public.web_config (category, key, value, description)
VALUES ('token_expiry', 'backend_refresh_token_expiry', '604800000', '后台 Refresh Token 有效期（毫秒）');
