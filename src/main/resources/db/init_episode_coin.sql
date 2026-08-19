-- 剧集投币表
CREATE TABLE IF NOT EXISTS public.episode_coin (
    id          BIGSERIAL PRIMARY KEY,
    episode_id  BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    coin_count  INTEGER      DEFAULT 1,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  public.episode_coin            IS '剧集投币表';
COMMENT ON COLUMN public.episode_coin.id          IS '主键id';
COMMENT ON COLUMN public.episode_coin.episode_id  IS '关联剧集ID';
COMMENT ON COLUMN public.episode_coin.user_id     IS '投币用户ID';
COMMENT ON COLUMN public.episode_coin.coin_count  IS '投币数量（默认1个）';
COMMENT ON COLUMN public.episode_coin.create_time IS '操作时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_episode_user_coin ON public.episode_coin(episode_id, user_id);
