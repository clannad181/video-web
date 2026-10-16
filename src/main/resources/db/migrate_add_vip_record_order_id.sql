-- ==================================================
-- 大会员开通记录表增加关联订单ID字段（幂等）
-- ==================================================

ALTER TABLE public.vip_record ADD COLUMN IF NOT EXISTS order_id BIGINT;
COMMENT ON COLUMN public.vip_record.order_id IS '关联订单ID（关联 order_info.id，管理端直接开通时为 null）';
