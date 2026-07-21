-- ==================================================
-- 用户信息表 (UserInfo)
-- 由 UserInfo 实体类生成
-- ==================================================

CREATE TABLE IF NOT EXISTS public.user_info (
    id          BIGSERIAL       PRIMARY KEY,
    user_name   VARCHAR(64)     NOT NULL,
    password    VARCHAR(256)    NOT NULL,
    email       VARCHAR(128),
    is_vip      BOOLEAN         DEFAULT FALSE,
    nike_name   VARCHAR(64),
    avatar      VARCHAR(512),
    type        SMALLINT        DEFAULT 0,
    role_id     VARCHAR(64),
    status      SMALLINT        DEFAULT 0,
    descript    VARCHAR(512),
    sex         BOOLEAN         DEFAULT TRUE,
    create_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT,
    update_time TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    is_delete   BOOLEAN         DEFAULT FALSE,
    version     INTEGER         DEFAULT 0,
    salt        VARCHAR(32)
);

-- 注释
COMMENT ON TABLE  public.user_info IS '用户信息表';
COMMENT ON COLUMN public.user_info.id          IS '主键id';
COMMENT ON COLUMN public.user_info.user_name   IS '用户名';
COMMENT ON COLUMN public.user_info.password    IS '用户密码';
COMMENT ON COLUMN public.user_info.email       IS '用户邮箱';
COMMENT ON COLUMN public.user_info.is_vip      IS '是否为大会员';
COMMENT ON COLUMN public.user_info.nike_name   IS '用户昵称';
COMMENT ON COLUMN public.user_info.avatar      IS '用户头像';
COMMENT ON COLUMN public.user_info.type        IS '用户类型（0-普通用户，1-管理员用户）';
COMMENT ON COLUMN public.user_info.role_id     IS '用户角色Id';
COMMENT ON COLUMN public.user_info.status      IS '用户状态（0-正常，1-禁用）';
COMMENT ON COLUMN public.user_info.descript    IS '用户个性签名';
COMMENT ON COLUMN public.user_info.sex         IS '性别';
COMMENT ON COLUMN public.user_info.create_time IS '创建时间';
COMMENT ON COLUMN public.user_info.create_user IS '创建人';
COMMENT ON COLUMN public.user_info.update_user IS '更新人';
COMMENT ON COLUMN public.user_info.update_time IS '更新时间';
COMMENT ON COLUMN public.user_info.is_delete   IS '是否删除（逻辑删除）';
COMMENT ON COLUMN public.user_info.version     IS '乐观锁版本号';
COMMENT ON COLUMN public.user_info.salt        IS '密码盐值（Base64编码的16字节随机盐）';
