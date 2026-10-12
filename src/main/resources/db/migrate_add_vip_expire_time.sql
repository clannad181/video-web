-- ==================================================
-- 用户表增加大会员到期时间字段（幂等）
-- ==================================================

ALTER TABLE public.user_info ADD COLUMN IF NOT EXISTS vip_expire_time TIMESTAMP;
COMMENT ON COLUMN public.user_info.vip_expire_time IS '大会员到期时间（非大会员为 null）';
