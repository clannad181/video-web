-- 字典类型表
CREATE TABLE IF NOT EXISTS public.dict_type (
    id          BIGSERIAL PRIMARY KEY,
    type_code   VARCHAR(64)  NOT NULL,
    type_name   VARCHAR(64)  NOT NULL,
    remark      VARCHAR(255),
    is_system   BOOLEAN      DEFAULT FALSE,
    status      BOOLEAN      DEFAULT TRUE,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    update_user BIGINT,
    is_delete   INTEGER      DEFAULT 0
);

COMMENT ON TABLE  public.dict_type IS '字典类型';
COMMENT ON COLUMN public.dict_type.id          IS '主键id';
COMMENT ON COLUMN public.dict_type.type_code   IS '字典类型编码（唯一）';
COMMENT ON COLUMN public.dict_type.type_name   IS '字典类型名称（唯一）';
COMMENT ON COLUMN public.dict_type.remark      IS '备注';
COMMENT ON COLUMN public.dict_type.is_system   IS '是否为系统内置字典（系统内置不可删除、不可修改编码）';
COMMENT ON COLUMN public.dict_type.status      IS '状态（true启用/false禁用）';
COMMENT ON COLUMN public.dict_type.create_time IS '创建时间';
COMMENT ON COLUMN public.dict_type.update_time IS '更新时间';
COMMENT ON COLUMN public.dict_type.update_user IS '更新人';
COMMENT ON COLUMN public.dict_type.is_delete   IS '是否删除';
