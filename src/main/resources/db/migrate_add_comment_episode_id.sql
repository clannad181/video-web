-- =============================================
-- 评论剧集维度迁移
-- 为 comment 表新增 episode_id 列，评论从视频级升级为剧集级
-- 旧数据（episode_id 为空）视为视频级历史评论
-- =============================================

ALTER TABLE IF EXISTS public.comment
    ADD COLUMN IF NOT EXISTS episode_id BIGINT;

COMMENT ON COLUMN public.comment.episode_id IS '所属剧集ID（为空表示视频级历史评论）';

-- 剧集评论查询索引
CREATE INDEX IF NOT EXISTS idx_comment_episode_time ON public.comment(episode_id, create_time) WHERE episode_id IS NOT NULL;
