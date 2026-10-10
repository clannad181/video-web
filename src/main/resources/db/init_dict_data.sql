-- 字典数据表
CREATE TABLE IF NOT EXISTS public.dict_data (
    id          BIGSERIAL PRIMARY KEY,
    type_code   VARCHAR(64)  NOT NULL,
    label       VARCHAR(64)  NOT NULL,
    value       VARCHAR(128) NOT NULL,
    sort        INTEGER      DEFAULT 0,
    is_default  BOOLEAN      DEFAULT FALSE,
    remark      VARCHAR(255),
    status      BOOLEAN      DEFAULT TRUE,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    update_user BIGINT,
    is_delete   INTEGER      DEFAULT 0
);

COMMENT ON TABLE  public.dict_data IS '字典数据';
COMMENT ON COLUMN public.dict_data.id          IS '主键id';
COMMENT ON COLUMN public.dict_data.type_code   IS '所属字典类型编码';
COMMENT ON COLUMN public.dict_data.label       IS '字典标签';
COMMENT ON COLUMN public.dict_data.value       IS '字典值';
COMMENT ON COLUMN public.dict_data.sort        IS '排序';
COMMENT ON COLUMN public.dict_data.is_default  IS '是否默认项';
COMMENT ON COLUMN public.dict_data.remark      IS '备注';
COMMENT ON COLUMN public.dict_data.status      IS '状态（true启用/false禁用）';
COMMENT ON COLUMN public.dict_data.create_time IS '创建时间';
COMMENT ON COLUMN public.dict_data.update_time IS '更新时间';
COMMENT ON COLUMN public.dict_data.update_user IS '更新人';
COMMENT ON COLUMN public.dict_data.is_delete   IS '是否删除';
