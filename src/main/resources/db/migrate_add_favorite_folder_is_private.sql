-- favorite_folder 增加 is_private 列（幂等，存量收藏夹默认为公开）
ALTER TABLE public.favorite_folder ADD COLUMN IF NOT EXISTS is_private SMALLINT DEFAULT 0;

COMMENT ON COLUMN public.favorite_folder.is_private IS '1-私密（仅本人可见），0-公开';
