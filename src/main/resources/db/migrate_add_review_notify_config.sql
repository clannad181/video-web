-- ============================================
-- 视频审核结果通知开关配置（存放在 web_config 表）
-- ============================================

-- 剧集审核通过/拒绝后是否向 UP 主发送消息通知（true-发送，false-不发送）
INSERT INTO public.web_config (category, key, value, description)
VALUES ('notification', 'videoReviewNotify', 'true', '剧集审核通过/拒绝后是否发送消息通知（true-发送，false-不发送）');
