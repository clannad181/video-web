-- 稿件举报表
CREATE TABLE IF NOT EXISTS public.report (
    id            BIGSERIAL PRIMARY KEY,
    video_id      BIGINT       NOT NULL,
    episode_id    BIGINT,
    reporter_id   BIGINT       NOT NULL,
    report_type   SMALLINT     NOT NULL,
    reason        VARCHAR(500),
    status        SMALLINT     DEFAULT 0,
    handler_id    BIGINT,
    handler_note  VARCHAR(500),
    handle_time   TIMESTAMP,
    create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  public.report              IS '稿件举报表';
COMMENT ON COLUMN public.report.id            IS '主键id';
COMMENT ON COLUMN public.report.video_id      IS '被举报视频ID';
COMMENT ON COLUMN public.report.episode_id    IS '被举报剧集ID（为空表示举报整个视频）';
COMMENT ON COLUMN public.report.reporter_id   IS '举报人用户ID';
COMMENT ON COLUMN public.report.report_type   IS '举报类型：1-色情低俗，2-政治敏感，3-暴恐血腥，4-虚假信息，5-侵权抄袭，6-恶意引战，7-其他';
COMMENT ON COLUMN public.report.reason        IS '举报补充说明';
COMMENT ON COLUMN public.report.status        IS '处理状态：0-待处理，1-已受理（确认违规），2-已驳回（未违规）';
COMMENT ON COLUMN public.report.handler_id    IS '处理人ID（管理员）';
COMMENT ON COLUMN public.report.handler_note  IS '处理备注';
COMMENT ON COLUMN public.report.handle_time   IS '处理时间';
COMMENT ON COLUMN public.report.create_time   IS '举报时间';
