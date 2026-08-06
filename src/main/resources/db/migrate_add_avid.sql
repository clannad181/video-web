-- =============================================
-- 视频avid字段迁移
-- 为 video_info 和 video_episode 表新增 avid 列
-- avid 格式：av + Base62编码的雪花ID，用于分享和指定集数跳转
-- =============================================

-- 1. video_info 表：新增 avid 列
ALTER TABLE IF EXISTS public.video_info
    ADD COLUMN IF NOT EXISTS avid VARCHAR(32);

COMMENT ON COLUMN public.video_info.avid IS '视频avid（av + Base62编码的雪花ID）';

-- 为 avid 创建唯一索引（确保唯一性）
CREATE UNIQUE INDEX IF NOT EXISTS idx_video_info_avid ON public.video_info(avid) WHERE avid IS NOT NULL;

-- 2. video_episode 表：新增 avid 列
ALTER TABLE IF EXISTS public.video_episode
    ADD COLUMN IF NOT EXISTS avid VARCHAR(32);

COMMENT ON COLUMN public.video_episode.avid IS '剧集avid（av + Base62编码的雪花ID，用于指定集数跳转）';

-- 为 avid 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_video_episode_avid ON public.video_episode(avid) WHERE avid IS NOT NULL;
