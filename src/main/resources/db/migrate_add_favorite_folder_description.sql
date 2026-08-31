-- favorite_folder 增加 description 列（幂等）
-- NULL 表示未设置描述
ALTER TABLE public.favorite_folder ADD COLUMN IF NOT EXISTS description VARCHAR(200);

COMMENT ON COLUMN public.favorite_folder.description IS '收藏夹描述（最长200字，NULL表示未设置）';
