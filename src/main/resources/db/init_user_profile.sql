-- =====================================================
-- 用户画像表 (user_profile)
-- 存储用户基于观看历史计算出的兴趣标签与分类及权重，
-- 由每日定时任务批量重建（派生数据，重建时物理删除后重写）
-- =====================================================
CREATE TABLE IF NOT EXISTS public.user_profile (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL,               -- 用户ID
    item_type           VARCHAR(20)     NOT NULL,               -- 兴趣维度：tag-标签 / category-分类
    item_id             BIGINT,                                 -- 分类ID（分类维度使用，标签维度为空）
    item_name           VARCHAR(100)    NOT NULL,               -- 标签名或分类名
    score               NUMERIC(12,2)   DEFAULT 0,              -- 兴趣得分（观看时长占比×时间衰减累加）
    watch_count         INTEGER         DEFAULT 0,              -- 观看视频数（去重后）
    total_watch_seconds BIGINT          DEFAULT 0,              -- 累计观看秒数
    last_watch_time     TIMESTAMP,                              -- 该维度最近观看时间
    create_time         TIMESTAMP       DEFAULT NOW(),          -- 创建时间
    update_time         TIMESTAMP       DEFAULT NOW()           -- 更新时间
);

-- 索引
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_profile_item    ON public.user_profile(user_id, item_type, item_name);
CREATE INDEX IF NOT EXISTS idx_user_profile_user_id      ON public.user_profile(user_id);
