-- 剧集收藏表
CREATE TABLE IF NOT EXISTS public.episode_favorite (
    id          BIGSERIAL PRIMARY KEY,
    episode_id  BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    status      SMALLINT     DEFAULT 1,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  public.episode_favorite            IS '剧集收藏表';
COMMENT ON COLUMN public.episode_favorite.id          IS '主键id';
COMMENT ON COLUMN public.episode_favorite.episode_id  IS '关联剧集ID';
COMMENT ON COLUMN public.episode_favorite.user_id     IS '收藏用户ID';
COMMENT ON COLUMN public.episode_favorite.status      IS '1-收藏，0-取消';
COMMENT ON COLUMN public.episode_favorite.create_time IS '操作时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_episode_user_favorite ON public.episode_favorite(episode_id, user_id);
