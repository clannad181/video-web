-- episode_favorite 增加 folder_id 列（幂等）
ALTER TABLE public.episode_favorite ADD COLUMN IF NOT EXISTS folder_id BIGINT;

-- 为每个有收藏记录的用户创建默认收藏夹（幂等：仅当用户尚无默认夹时创建）
INSERT INTO public.favorite_folder (avid, user_id, name, is_default, create_time)
SELECT 'fd' || UPPER(SUBSTRING(md5(u.user_id::text) FROM 1 FOR 16)),
       u.user_id,
       '默认收藏夹',
       1,
       CURRENT_TIMESTAMP
FROM (SELECT DISTINCT user_id FROM public.episode_favorite WHERE folder_id IS NULL) u
WHERE NOT EXISTS (
    SELECT 1 FROM public.favorite_folder ff
    WHERE ff.user_id = u.user_id AND ff.is_default = 1 AND ff.is_delete = 0
);

-- 回填存量收藏记录到对应用户的默认文件夹
UPDATE public.episode_favorite ef
SET folder_id = ff.id
FROM public.favorite_folder ff
WHERE ef.folder_id IS NULL
  AND ff.user_id = ef.user_id
  AND ff.is_default = 1
  AND ff.is_delete = 0;

-- 设置非空约束
ALTER TABLE public.episode_favorite ALTER COLUMN folder_id SET NOT NULL;

COMMENT ON COLUMN public.episode_favorite.folder_id IS '所属收藏文件夹ID';

-- 唯一索引调整：由 (episode_id, user_id) 变更为 (episode_id, user_id, folder_id)
DROP INDEX IF EXISTS public.uk_episode_user_favorite;
CREATE UNIQUE INDEX IF NOT EXISTS uk_episode_user_folder_favorite ON public.episode_favorite(episode_id, user_id, folder_id);
