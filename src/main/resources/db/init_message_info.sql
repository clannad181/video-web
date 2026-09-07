-- 消息通知表
CREATE TABLE IF NOT EXISTS public.message_info (
    id            BIGSERIAL PRIMARY KEY,
    avid          VARCHAR(64)  NOT NULL,
    user_id       BIGINT       NOT NULL,
    type          SMALLINT     NOT NULL,
    title         VARCHAR(200),
    content       TEXT,
    biz_id        BIGINT,
    biz_avid      VARCHAR(64),
    play_link     VARCHAR(255),
    is_read       SMALLINT     DEFAULT 0,
    read_time     TIMESTAMP,
    create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_delete     SMALLINT     DEFAULT 0
);

COMMENT ON TABLE  public.message_info            IS '消息通知表';
COMMENT ON COLUMN public.message_info.id          IS '主键id';
COMMENT ON COLUMN public.message_info.avid        IS '消息唯一标识（av + Base62编码的雪花ID）';
COMMENT ON COLUMN public.message_info.user_id     IS '接收者用户ID';
COMMENT ON COLUMN public.message_info.type        IS '消息类型：1-视频发布成功，2-稿件审核通知，3-举报下架通知，4-服务器状态通知';
COMMENT ON COLUMN public.message_info.title       IS '消息标题';
COMMENT ON COLUMN public.message_info.content     IS '消息正文';
COMMENT ON COLUMN public.message_info.biz_id      IS '关联业务ID（如视频ID）';
COMMENT ON COLUMN public.message_info.biz_avid    IS '关联业务avid（如视频avid）';
COMMENT ON COLUMN public.message_info.play_link   IS '播放跳转链接（如 /client/video/avXxx/episode/avYyy），服务器状态通知为 null';
COMMENT ON COLUMN public.message_info.is_read     IS '是否已读：0-未读，1-已读';
COMMENT ON COLUMN public.message_info.read_time   IS '阅读时间';
COMMENT ON COLUMN public.message_info.create_time IS '消息创建时间';
COMMENT ON COLUMN public.message_info.is_delete   IS '逻辑删除：0-未删除，1-已删除';

-- 已存在的 message_info 表补充 play_link 列（幂等）
ALTER TABLE public.message_info ADD COLUMN IF NOT EXISTS play_link VARCHAR(255);
COMMENT ON COLUMN public.message_info.play_link IS '播放跳转链接（如 /client/video/avXxx/episode/avYyy），服务器状态通知为 null';
