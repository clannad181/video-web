-- 评论点赞表
CREATE TABLE IF NOT EXISTS public.comment_like (
    id          BIGSERIAL PRIMARY KEY,
    comment_id  BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    status      SMALLINT     DEFAULT 1,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  public.comment_like            IS '评论点赞表';
COMMENT ON COLUMN public.comment_like.id          IS '主键id';
COMMENT ON COLUMN public.comment_like.comment_id  IS '关联评论ID';
COMMENT ON COLUMN public.comment_like.user_id     IS '点赞用户ID';
COMMENT ON COLUMN public.comment_like.status      IS '1-点赞，0-取消';
COMMENT ON COLUMN public.comment_like.create_time IS '操作时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_comment_user ON public.comment_like(comment_id, user_id);
