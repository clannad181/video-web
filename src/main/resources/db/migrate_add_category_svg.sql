-- =============================================
-- 分类SVG图标字段迁移
-- 为 category 表新增 svg 列，用于展示各分类的SVG图标
-- =============================================

ALTER TABLE IF EXISTS public.category
    ADD COLUMN IF NOT EXISTS svg TEXT;

COMMENT ON COLUMN public.category.svg IS 'SVG图标（前端展示分类图标使用）';
