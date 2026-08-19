-- 剧集点赞表
CREATE TABLE IF NOT EXISTS public.episode_like (
    id          BIGSERIAL PRIMARY KEY,
    episode_id  BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    status      SMALLINT     DEFAULT 1,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  public.episode_like            IS '剧集点赞表';
COMMENT ON COLUMN public.episode_like.id          IS '主键id';
COMMENT ON COLUMN public.episode_like.episode_id  IS '关联剧集ID';
COMMENT ON COLUMN public.episode_like.user_id     IS '点赞用户ID';
COMMENT ON COLUMN public.episode_like.status      IS '1-点赞，0-取消';
COMMENT ON COLUMN public.episode_like.create_time IS '操作时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_episode_user_like ON public.episode_like(episode_id, user_id);
