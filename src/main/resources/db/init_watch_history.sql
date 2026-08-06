-- =====================================================
-- 观看历史记录表 (watch_history)
-- 记录用户观看视频的进度，包含观看到的具体秒数
-- =====================================================
CREATE TABLE IF NOT EXISTS public.watch_history (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,                   -- 用户ID
    video_id        BIGINT          NOT NULL,                   -- 视频ID
    episode_id      BIGINT,                                     -- 剧集ID（单集视频为空）
    watched_seconds BIGINT          DEFAULT 0,                  -- 已观看秒数
    play_link       VARCHAR(500),                                -- 播放跳转链接
    watch_time      TIMESTAMP       DEFAULT NOW(),              -- 最近观看时间
    create_time     TIMESTAMP       DEFAULT NOW(),              -- 记录创建时间
    is_delete       INTEGER         DEFAULT 0                   -- 逻辑删除：0-未删除，1-已删除
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_watch_history_user_id        ON public.watch_history(user_id);
CREATE INDEX IF NOT EXISTS idx_watch_history_user_video      ON public.watch_history(user_id, video_id);
CREATE INDEX IF NOT EXISTS idx_watch_history_watch_time      ON public.watch_history(watch_time);

COMMENT ON TABLE  public.watch_history                IS '观看历史记录表';
COMMENT ON COLUMN public.watch_history.id             IS '主键ID';
COMMENT ON COLUMN public.watch_history.user_id        IS '用户ID';
COMMENT ON COLUMN public.watch_history.video_id       IS '视频ID';
COMMENT ON COLUMN public.watch_history.episode_id     IS '剧集ID（单集视频为空）';
COMMENT ON COLUMN public.watch_history.watched_seconds IS '已观看秒数';
COMMENT ON COLUMN public.watch_history.play_link       IS '播放跳转链接（如 /admin/video/avXxx/episode/avYyy）';
COMMENT ON COLUMN public.watch_history.watch_time     IS '最近观看时间';
COMMENT ON COLUMN public.watch_history.create_time    IS '记录创建时间';
COMMENT ON COLUMN public.watch_history.is_delete      IS '逻辑删除：0-未删除，1-已删除';
