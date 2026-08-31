-- user_info 增加 birthday 列（幂等）
ALTER TABLE IF EXISTS public.user_info
    ADD COLUMN IF NOT EXISTS birthday DATE;

COMMENT ON COLUMN public.user_info.birthday IS '生日';
