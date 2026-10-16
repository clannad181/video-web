-- ==================================================
-- 商品表 (Goods)
-- 商品分类复用字典体系（dict_type.type_code = 'goods_category'），分类项由字典数据接口维护
-- ==================================================

CREATE TABLE IF NOT EXISTS public.goods (
    id              BIGSERIAL       PRIMARY KEY,
    category_id     BIGINT          NOT NULL,
    name            VARCHAR(128)    NOT NULL,
    original_price  NUMERIC(10,2)   NOT NULL,
    discount_price  NUMERIC(10,2),
    cover           VARCHAR(512),
    images          TEXT,
    description     TEXT,
    status          SMALLINT        DEFAULT 1,
    sort            INTEGER         DEFAULT 0,
    create_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    create_user     BIGINT,
    update_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_user     BIGINT,
    is_delete       SMALLINT        DEFAULT 0
);

-- 注释
COMMENT ON TABLE  public.goods                  IS '商品表';
COMMENT ON COLUMN public.goods.id               IS '主键id';
COMMENT ON COLUMN public.goods.category_id      IS '商品分类（关联 dict_data.id，type_code=goods_category）';
COMMENT ON COLUMN public.goods.name             IS '商品名称';
COMMENT ON COLUMN public.goods.original_price   IS '原价（元，保留2位小数）';
COMMENT ON COLUMN public.goods.discount_price   IS '优惠价（元，保留2位小数，null 表示无优惠）';
COMMENT ON COLUMN public.goods.cover            IS '商品主图URL';
COMMENT ON COLUMN public.goods.images           IS '商品详情图URL列表（逗号分隔）';
COMMENT ON COLUMN public.goods.description      IS '商品描述';
COMMENT ON COLUMN public.goods.status           IS '商品状态：0-下架，1-上架';
COMMENT ON COLUMN public.goods.sort             IS '排序（越小越靠前）';
COMMENT ON COLUMN public.goods.create_time      IS '创建时间';
COMMENT ON COLUMN public.goods.create_user      IS '创建人（操作管理员ID）';
COMMENT ON COLUMN public.goods.update_time      IS '更新时间';
COMMENT ON COLUMN public.goods.update_user      IS '更新人';
COMMENT ON COLUMN public.goods.is_delete        IS '是否删除：0-未删除，1-已删除（逻辑删除）';

-- 商品分类字典类型初始化（幂等，系统内置：不可删除、不可修改编码）
INSERT INTO public.dict_type (type_code, type_name, remark, is_system, status)
SELECT 'goods_category', '商品分类', '商品管理模块的商品分类，分类项由字典数据管理维护', TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM public.dict_type WHERE type_code = 'goods_category');
