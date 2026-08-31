-- favorite_folder 增加 cover_url 列（幂等）
-- NULL 表示未设置自定义封面，接口返回时回退为夹内最新收藏视频封面
ALTER TABLE public.favorite_folder ADD COLUMN IF NOT EXISTS cover_url VARCHAR(500);

COMMENT ON COLUMN public.favorite_folder.cover_url IS '自定义封面URL（NULL表示未设置，使用最新收藏视频封面）';
