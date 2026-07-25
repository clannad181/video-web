-- 视频信息表
CREATE TABLE IF NOT EXISTS public.video_info (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    category_id     BIGINT,
    cover_url       VARCHAR(500),
    total_duration  BIGINT       DEFAULT 0,
    episode_count   INTEGER      DEFAULT 0,
    play_count      BIGINT       DEFAULT 0,
    favorite_count  BIGINT       DEFAULT 0,
    like_count      BIGINT       DEFAULT 0,
    coin_count      BIGINT       DEFAULT 0,
    viewer_count    BIGINT       DEFAULT 0,
    user_id         BIGINT       NOT NULL,
    user_name       VARCHAR(100),
    nick_name       VARCHAR(100),
    tags            TEXT,
    review_status   INTEGER      DEFAULT 0,
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
COMMENT ON COLUMN public.video_info.play_count     IS '播放数';
COMMENT ON COLUMN public.video_info.favorite_count IS '收藏数';
COMMENT ON COLUMN public.video_info.like_count     IS '点赞数';
COMMENT ON COLUMN public.video_info.coin_count     IS '投币数';
COMMENT ON COLUMN public.video_info.viewer_count   IS '观看人数';
COMMENT ON COLUMN public.video_info.user_id        IS 'UP主用户id';
COMMENT ON COLUMN public.video_info.user_name      IS 'UP主用户名';
COMMENT ON COLUMN public.video_info.nick_name      IS 'UP主昵称';
COMMENT ON COLUMN public.video_info.tags           IS '标签，多个以逗号分隔';
COMMENT ON COLUMN public.video_info.review_status  IS '审核状态：0-待审核，1-审核通过，2-审核拒绝';
COMMENT ON COLUMN public.video_info.create_time    IS '创建时间';
COMMENT ON COLUMN public.video_info.update_time    IS '更新时间';
COMMENT ON COLUMN public.video_info.is_delete      IS '是否删除';
