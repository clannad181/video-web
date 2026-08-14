-- =====================================================
-- 用户关注关系表 (user_follow)
-- 记录用户关注的UP主（UP主即被关注的用户）
-- =====================================================
CREATE TABLE IF NOT EXISTS public.user_follow (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,                   -- 关注者用户ID
    follow_user_id  BIGINT          NOT NULL,                   -- 被关注的UP主用户ID
    create_time     TIMESTAMP       DEFAULT NOW(),              -- 关注时间
    is_delete       INTEGER         DEFAULT 0                   -- 逻辑删除：0-关注中，1-已取关
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_user_follow_user_id          ON public.user_follow(user_id);
CREATE INDEX IF NOT EXISTS idx_user_follow_follow_user_id   ON public.user_follow(follow_user_id);
-- 部分唯一索引：同一对关注关系仅允许一条有效记录
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_follow_pair       ON public.user_follow(user_id, follow_user_id) WHERE is_delete = 0;
