-- 视频剧集表
CREATE TABLE IF NOT EXISTS public.video_episode (
    id              BIGSERIAL PRIMARY KEY,
    video_id        BIGINT       NOT NULL,
    episode_number  INTEGER      NOT NULL,
    title           VARCHAR(255),
    duration        BIGINT       DEFAULT 0,
    play_url        VARCHAR(1000),
    sort            INTEGER      DEFAULT 0,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_delete       INTEGER      DEFAULT 0
);

COMMENT ON TABLE  public.video_episode IS '视频剧集';
COMMENT ON COLUMN public.video_episode.id             IS '主键id';
COMMENT ON COLUMN public.video_episode.video_id       IS '视频id';
COMMENT ON COLUMN public.video_episode.episode_number IS '集数序号';
COMMENT ON COLUMN public.video_episode.title          IS '剧集标题';
COMMENT ON COLUMN public.video_episode.duration       IS '剧集时长（秒）';
COMMENT ON COLUMN public.video_episode.play_url       IS '播放地址';
COMMENT ON COLUMN public.video_episode.sort           IS '排序';
COMMENT ON COLUMN public.video_episode.create_time    IS '创建时间';
COMMENT ON COLUMN public.video_episode.is_delete      IS '是否删除';
