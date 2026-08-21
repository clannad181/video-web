-- 收藏文件夹表
CREATE TABLE IF NOT EXISTS public.favorite_folder (
    id          BIGSERIAL PRIMARY KEY,
    avid        VARCHAR(32)  NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    is_default  SMALLINT     DEFAULT 0,
    is_private  SMALLINT     DEFAULT 0,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    is_delete   SMALLINT     DEFAULT 0
);

COMMENT ON TABLE  public.favorite_folder              IS '收藏文件夹表';
COMMENT ON COLUMN public.favorite_folder.id           IS '主键id';
COMMENT ON COLUMN public.favorite_folder.avid         IS '文件夹前台标识（fd前缀Base62编码）';
COMMENT ON COLUMN public.favorite_folder.user_id      IS '所属用户ID';
COMMENT ON COLUMN public.favorite_folder.name         IS '文件夹名称';
COMMENT ON COLUMN public.favorite_folder.is_default   IS '1-默认文件夹，0-自定义文件夹';
COMMENT ON COLUMN public.favorite_folder.is_private   IS '1-私密（仅本人可见），0-公开';
COMMENT ON COLUMN public.favorite_folder.create_time  IS '创建时间';
COMMENT ON COLUMN public.favorite_folder.update_time  IS '更新时间';
COMMENT ON COLUMN public.favorite_folder.is_delete    IS '是否删除';

-- 每用户仅一个未删除的默认文件夹
CREATE UNIQUE INDEX IF NOT EXISTS uk_folder_user_default ON public.favorite_folder(user_id) WHERE is_default = 1 AND is_delete = 0;
