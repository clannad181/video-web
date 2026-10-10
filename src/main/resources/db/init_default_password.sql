-- ============================================
-- 密码重置默认密码配置（存放在 web_config 表）
-- ============================================

-- 管理员重置用户密码时使用的默认密码（security 分类，default_password 键）
INSERT INTO public.web_config (category, key, value, description)
VALUES ('security', 'default_password', '123456', '管理员重置用户密码时使用的默认密码');
