-- =====================================================
-- 登录日志表 (login_log)
-- 通过 AOP 切面自动记录每次登录行为
-- =====================================================
CREATE TABLE IF NOT EXISTS public.login_log (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT,                         -- 用户ID（通过账号反查，找不到则为空）
    account         VARCHAR(255),                   -- 登录账号（用户名或邮箱）
    login_type      VARCHAR(50),                    -- 登录方式：img_captcha / email_captcha
    system_type     INTEGER         DEFAULT 1,      -- 系统类型：1-后台管理，0-前台用户
    ip_address      VARCHAR(128),                   -- 登录IP地址
    user_agent      VARCHAR(512),                   -- 浏览器UserAgent
    login_status    INTEGER         DEFAULT 1,      -- 登录状态：0-失败，1-成功
    fail_reason     VARCHAR(500),                   -- 失败原因
    login_time      TIMESTAMP,                      -- 登录时间
    create_time     TIMESTAMP       DEFAULT NOW(),  -- 记录创建时间
    is_delete       INTEGER         DEFAULT 0       -- 逻辑删除：0-未删除，1-已删除
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_login_log_user_id    ON public.login_log(user_id);
CREATE INDEX IF NOT EXISTS idx_login_log_account    ON public.login_log(account);
CREATE INDEX IF NOT EXISTS idx_login_log_login_time ON public.login_log(login_time);
CREATE INDEX IF NOT EXISTS idx_login_log_status     ON public.login_log(login_status);

COMMENT ON TABLE  public.login_log             IS '登录日志表';
COMMENT ON COLUMN public.login_log.id          IS '主键ID';
COMMENT ON COLUMN public.login_log.user_id     IS '用户ID';
COMMENT ON COLUMN public.login_log.account     IS '登录账号（用户名或邮箱）';
COMMENT ON COLUMN public.login_log.login_type  IS '登录方式：img_captcha-图片验证码，email_captcha-邮箱验证码';
COMMENT ON COLUMN public.login_log.system_type  IS '系统类型：1-后台管理，0-前台用户';
COMMENT ON COLUMN public.login_log.ip_address  IS '登录IP地址';
COMMENT ON COLUMN public.login_log.user_agent  IS '浏览器UserAgent';
COMMENT ON COLUMN public.login_log.login_status IS '登录状态：0-失败，1-成功';
COMMENT ON COLUMN public.login_log.fail_reason  IS '失败原因';
COMMENT ON COLUMN public.login_log.login_time   IS '登录时间';
COMMENT ON COLUMN public.login_log.create_time  IS '记录创建时间';
COMMENT ON COLUMN public.login_log.is_delete    IS '逻辑删除：0-未删除，1-已删除';
