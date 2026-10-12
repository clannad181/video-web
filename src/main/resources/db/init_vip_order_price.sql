-- ============================================
-- 大会员充值定价配置（存放在 web_config 表）
-- 值单位：元/月
-- ============================================

INSERT INTO public.web_config (category, key, value, description)
SELECT 'vip', 'vip_price_per_month', '25', '大会员月单价（元）'
WHERE NOT EXISTS (
    SELECT 1 FROM public.web_config
    WHERE category = 'vip' AND key = 'vip_price_per_month' AND is_delete = 0
);
