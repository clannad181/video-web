-- ==================================================
-- 大会员开通记录表增加权益区间起点字段（幂等）
-- start_time 记录开通时的累加基准，撤销记录时用于识别并联动调整下游记录
-- ==================================================

ALTER TABLE public.vip_record ADD COLUMN IF NOT EXISTS start_time TIMESTAMP;

COMMENT ON COLUMN public.vip_record.start_time IS '权益区间起点（开通时累加基准：已是大会员时为原到期时间，否则为开通时间；撤销记录时用于联动调整下游记录）';

-- 回填历史数据（近似）：同一用户按到期时间升序排列，
-- 非首笔记录若创建时前一笔尚未到期（连续累加开通），起点取前一笔到期时间；否则取创建时间
UPDATE public.vip_record vr
SET start_time = sub.start_value
FROM (
    SELECT id,
           CASE
               WHEN prev_expire IS NOT NULL AND prev_expire > create_time THEN prev_expire
               ELSE create_time
           END AS start_value
    FROM (
        SELECT id, create_time,
               LAG(expire_time) OVER (PARTITION BY user_id ORDER BY expire_time, id) AS prev_expire
        FROM public.vip_record
        WHERE is_delete = 0
    ) t
) sub
WHERE vr.id = sub.id
  AND vr.is_delete = 0
  AND vr.start_time IS NULL;
