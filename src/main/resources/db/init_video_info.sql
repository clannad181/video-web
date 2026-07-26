-- 视频信息表
CREATE TABLE IF NOT EXISTS public.video_info (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    category_id     BIGINT,
    cover_url       VARCHAR(500),
    total_duration  BIGINT       DEFAULT 0,
    episode_count   INTEGER      DEFAULT 0,
    user_id         BIGINT       NOT NULL,
    tags            TEXT,
    is_ongoing      BOOLEAN      DEFAULT FALSE,
    video_status    INTEGER      DEFAULT 0,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP,
    is_delete       INTEGER      DEFAULT 0
);

COMMENT ON TABLE  public.video_info IS '视频信息';
COMMENT ON COLUMN public.video_info.id             IS '主键id';
COMMENT ON COLUMN public.video_info.title          IS '视频标题';
COMMENT ON COLUMN public.video_info.description    IS '视频简介';
COMMENT ON COLUMN public.video_info.category_id    IS '分类id';
COMMENT ON COLUMN public.video_info.cover_url      IS '封面图URL';
COMMENT ON COLUMN public.video_info.total_duration IS '总时长（秒，多集累加）';
COMMENT ON COLUMN public.video_info.episode_count   IS '总集数';
COMMENT ON COLUMN public.video_info.user_id        IS 'UP主用户id';
COMMENT ON COLUMN public.video_info.tags           IS '标签，多个以逗号分隔';
COMMENT ON COLUMN public.video_info.is_ongoing     IS '是否连载中';
COMMENT ON COLUMN public.video_info.video_status   IS '视频状态：0-草稿，1-待发布，2-已发布，3-已下架';
COMMENT ON COLUMN public.video_info.create_time    IS '创建时间';
COMMENT ON COLUMN public.video_info.update_time    IS '更新时间';
COMMENT ON COLUMN public.video_info.is_delete      IS '是否删除';
