-- 视频分类表
CREATE TABLE IF NOT EXISTS public.category (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    sort        INTEGER      DEFAULT 0,
    status      BOOLEAN      DEFAULT TRUE,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    update_user BIGINT,
    is_delete   INTEGER      DEFAULT 0
);

COMMENT ON TABLE  public.category IS '视频分类';
COMMENT ON COLUMN public.category.id          IS '主键id';
COMMENT ON COLUMN public.category.name        IS '分类名称（如番剧、国创）';
COMMENT ON COLUMN public.category.sort        IS '排序';
COMMENT ON COLUMN public.category.status      IS '状态（true启用/false禁用）';
COMMENT ON COLUMN public.category.create_time IS '创建时间';
COMMENT ON COLUMN public.category.update_time IS '更新时间';
COMMENT ON COLUMN public.category.update_user IS '更新人';
COMMENT ON COLUMN public.category.is_delete   IS '是否删除';
