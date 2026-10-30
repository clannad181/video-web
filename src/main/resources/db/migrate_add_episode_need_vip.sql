-- ==================================================
-- 剧集表增加是否需要大会员观看字段（幂等）
-- ==================================================

ALTER TABLE public.video_episode ADD COLUMN IF NOT EXISTS need_vip BOOLEAN DEFAULT FALSE;
COMMENT ON COLUMN public.video_episode.need_vip IS '是否需要大会员观看';
