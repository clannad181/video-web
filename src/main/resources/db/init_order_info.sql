-- ==================================================
-- 通用订单表 (OrderInfo)
-- biz_type 区分业务类型（1-大会员充值），后续新增付费业务复用本表
-- ==================================================

CREATE TABLE IF NOT EXISTS public.order_info (
    id              BIGSERIAL       PRIMARY KEY,
    order_no        VARCHAR(64)     NOT NULL,
    user_id         BIGINT          NOT NULL,
    biz_type        SMALLINT        NOT NULL,
    amount          NUMERIC(10,2)   NOT NULL,
    duration_months INTEGER,
    status          SMALLINT        DEFAULT 0,
    pay_method      SMALLINT,
    pay_time        TIMESTAMP,
    expire_time     TIMESTAMP,
    remark          VARCHAR(256),
    create_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    create_user     BIGINT,
    update_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_user     BIGINT,
    is_delete       SMALLINT        DEFAULT 0
);

-- 订单号唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS uk_order_info_order_no ON public.order_info (order_no);

-- 注释
COMMENT ON TABLE  public.order_info                IS '通用订单表';
COMMENT ON COLUMN public.order_info.id             IS '主键id';
COMMENT ON COLUMN public.order_info.order_no       IS '订单号（on + Base62编码的雪花ID，唯一）';
COMMENT ON COLUMN public.order_info.user_id        IS '下单用户ID（关联 user_info.id）';
COMMENT ON COLUMN public.order_info.biz_type       IS '业务类型：1-大会员充值（预留后续业务类型）';
COMMENT ON COLUMN public.order_info.amount         IS '订单金额（元）';
COMMENT ON COLUMN public.order_info.duration_months IS '大会员充值月数（其他业务为 null）';
COMMENT ON COLUMN public.order_info.status         IS '订单状态：0-待支付，1-已支付，2-已取消，3-已关闭（超时未支付）';
COMMENT ON COLUMN public.order_info.pay_method     IS '支付方式：1-支付宝，2-微信（未支付为 null）';
COMMENT ON COLUMN public.order_info.pay_time       IS '支付时间（未支付为 null）';
COMMENT ON COLUMN public.order_info.expire_time    IS '订单支付截止时间（超时未支付自动关闭）';
COMMENT ON COLUMN public.order_info.remark         IS '订单备注';
COMMENT ON COLUMN public.order_info.create_time    IS '创建时间';
COMMENT ON COLUMN public.order_info.create_user    IS '创建人（下单用户ID）';
COMMENT ON COLUMN public.order_info.update_time    IS '更新时间';
COMMENT ON COLUMN public.order_info.update_user    IS '更新人';
COMMENT ON COLUMN public.order_info.is_delete      IS '是否删除：0-未删除，1-已删除（逻辑删除）';
