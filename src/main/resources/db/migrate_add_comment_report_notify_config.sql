-- ============================================
-- 评论通知与举报处理结果通知开关配置（存放在 web_config 表）
-- ============================================

-- 评论通知开关：视频收到新评论（含评论审核通过）时是否通知 UP 主
INSERT INTO public.web_config (category, key, value, description)
VALUES ('notification', 'commentNotify', 'true', '视频收到新评论（含评论审核通过）时是否通知UP主（true-发送，false-不发送）');

-- 举报处理结果通知开关：举报处理完成（受理/驳回）后是否通知举报人
INSERT INTO public.web_config (category, key, value, description)
VALUES ('notification', 'reportNotify', 'true', '举报处理完成（受理/驳回）后是否通知举报人（true-发送，false-不发送）');
