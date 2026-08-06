-- 评论表
CREATE TABLE IF NOT EXISTS public.comment (
    id          BIGSERIAL PRIMARY KEY,
    video_id    BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    parent_id   BIGINT       DEFAULT 0,
    root_id     BIGINT       DEFAULT 0,
    content     TEXT         NOT NULL,
    like_count  INT          DEFAULT 0,
    reply_count INT          DEFAULT 0,
    status      SMALLINT     DEFAULT 0,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  public.comment            IS '评论表';
COMMENT ON COLUMN public.comment.id          IS '主键id';
COMMENT ON COLUMN public.comment.video_id    IS '所属视频ID';
COMMENT ON COLUMN public.comment.user_id     IS '评论者用户ID';
COMMENT ON COLUMN public.comment.parent_id   IS '父评论ID，一级评论为0';
COMMENT ON COLUMN public.comment.root_id     IS '根评论ID，一级评论root_id=id';
COMMENT ON COLUMN public.comment.content     IS '评论正文';
COMMENT ON COLUMN public.comment.like_count  IS '点赞数冗余';
COMMENT ON COLUMN public.comment.reply_count IS '直接子评论数冗余';
COMMENT ON COLUMN public.comment.status      IS '0-正常，1-用户删除，2-运营屏蔽，3-待审核';
COMMENT ON COLUMN public.comment.create_time IS '创建时间';
COMMENT ON COLUMN public.comment.update_time IS '更新时间';

-- 一级评论排序索引
CREATE INDEX IF NOT EXISTS idx_comment_video_root_time ON public.comment(video_id, root_id, create_time) WHERE parent_id = 0;
-- 用户查自己评论
CREATE INDEX IF NOT EXISTS idx_comment_user_time ON public.comment(user_id, create_time);
-- 查询子回复
CREATE INDEX IF NOT EXISTS idx_comment_root_parent ON public.comment(root_id, parent_id, id);
