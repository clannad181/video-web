-- ==================================================
-- 大会员开通记录表 (VipRecord)
-- 管理端开通/续期/撤销均写入记录，为前台开通功能预留扩展
-- ==================================================

CREATE TABLE IF NOT EXISTS public.vip_record (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    duration_months INTEGER,
    expire_time     TIMESTAMP       NOT NULL,
    status          SMALLINT        DEFAULT 0,
    remark          VARCHAR(256),
    create_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    create_user     BIGINT,
    update_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_user     BIGINT,
    is_delete       SMALLINT        DEFAULT 0
);

-- 注释
COMMENT ON TABLE  public.vip_record                IS '大会员开通记录表';
COMMENT ON COLUMN public.vip_record.id             IS '主键id';
COMMENT ON COLUMN public.vip_record.user_id        IS '用户ID（关联 user_info.id）';
COMMENT ON COLUMN public.vip_record.duration_months IS '开通时长（月，指定到期时间开通时为 null）';
COMMENT ON COLUMN public.vip_record.expire_time    IS '大会员到期时间';
COMMENT ON COLUMN public.vip_record.status         IS '记录状态：0-生效中，1-已到期（定时任务置为1）';
COMMENT ON COLUMN public.vip_record.remark         IS '开通备注';
COMMENT ON COLUMN public.vip_record.create_time    IS '创建时间';
COMMENT ON COLUMN public.vip_record.create_user    IS '创建人（操作管理员ID）';
COMMENT ON COLUMN public.vip_record.update_time    IS '更新时间';
COMMENT ON COLUMN public.vip_record.update_user    IS '更新人';
COMMENT ON COLUMN public.vip_record.is_delete      IS '是否删除：0-未删除，1-已撤销（逻辑删除）';
