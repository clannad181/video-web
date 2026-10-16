-- ==================================================
-- 大会员开通记录撤销方式调整：由逻辑删除改为状态标记（幂等）
-- 历史已撤销记录（is_delete=1）转换为状态 2-已撤销，并恢复为未删除
-- ==================================================

UPDATE public.vip_record SET status = 2, is_delete = 0 WHERE is_delete = 1;

COMMENT ON COLUMN public.vip_record.status IS '记录状态：0-生效中，1-已到期（定时任务置为1），2-已撤销（管理端撤销）';
COMMENT ON COLUMN public.vip_record.is_delete IS '是否删除：0-未删除，1-已删除（撤销改用 status=2 状态标记，该字段预留）';
