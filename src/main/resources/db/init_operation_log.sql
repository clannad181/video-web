-- =====================================================
-- 运行日志表 (operation_log)
-- 通过 AOP 切面自动记录操作日志和系统异常
-- =====================================================
CREATE TABLE IF NOT EXISTS public.operation_log (
    id                BIGSERIAL       PRIMARY KEY,
    user_id           BIGINT,                          -- 操作用户ID
    username          VARCHAR(255),                    -- 操作用户名（从JWT解析）
    module            VARCHAR(100),                    -- 所属模块
    operation         VARCHAR(255),                    -- 操作描述
    method            VARCHAR(255),                    -- 执行方法（类名.方法名）
    request_uri       VARCHAR(512),                    -- 请求URI
    request_method    VARCHAR(10),                     -- 请求方式：GET/POST/PUT/DELETE
    request_params    TEXT,                            -- 请求参数（JSON）
    ip_address        VARCHAR(128),                    -- 请求IP地址
    execution_time    BIGINT,                          -- 执行耗时（毫秒）
    result_status     INTEGER         DEFAULT 0,       -- 执行状态：0-失败，1-成功
    error_message     VARCHAR(2000),                   -- 错误信息
    error_stack_trace TEXT,                            -- 异常堆栈
    log_type          INTEGER         DEFAULT 1,       -- 日志类型：0-系统异常，1-业务操作
    system_type       INTEGER         DEFAULT 1,       -- 系统类型：1-后台管理，0-前台用户
    operation_time    TIMESTAMP,                       -- 操作时间
    create_time       TIMESTAMP       DEFAULT NOW(),   -- 创建时间
    is_delete         INTEGER         DEFAULT 0        -- 逻辑删除：0-未删除，1-已删除
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_operation_log_username      ON public.operation_log(username);
CREATE INDEX IF NOT EXISTS idx_operation_log_module        ON public.operation_log(module);
CREATE INDEX IF NOT EXISTS idx_operation_log_log_type      ON public.operation_log(log_type);
CREATE INDEX IF NOT EXISTS idx_operation_log_result_status ON public.operation_log(result_status);
CREATE INDEX IF NOT EXISTS idx_operation_log_system_type   ON public.operation_log(system_type);
CREATE INDEX IF NOT EXISTS idx_operation_log_op_time       ON public.operation_log(operation_time);

COMMENT ON TABLE  public.operation_log               IS '运行日志表';
COMMENT ON COLUMN public.operation_log.id             IS '主键ID';
COMMENT ON COLUMN public.operation_log.user_id        IS '操作用户ID';
COMMENT ON COLUMN public.operation_log.username       IS '操作用户名（从JWT解析）';
COMMENT ON COLUMN public.operation_log.module         IS '所属模块';
COMMENT ON COLUMN public.operation_log.operation      IS '操作描述';
COMMENT ON COLUMN public.operation_log.method         IS '执行方法（类名.方法名）';
COMMENT ON COLUMN public.operation_log.request_uri    IS '请求URI';
COMMENT ON COLUMN public.operation_log.request_method IS '请求方式：GET/POST/PUT/DELETE';
COMMENT ON COLUMN public.operation_log.request_params IS '请求参数（JSON）';
COMMENT ON COLUMN public.operation_log.ip_address     IS '请求IP地址';
COMMENT ON COLUMN public.operation_log.execution_time IS '执行耗时（毫秒）';
COMMENT ON COLUMN public.operation_log.result_status  IS '执行状态：0-失败，1-成功';
COMMENT ON COLUMN public.operation_log.error_message  IS '错误信息';
COMMENT ON COLUMN public.operation_log.error_stack_trace IS '异常堆栈';
COMMENT ON COLUMN public.operation_log.log_type       IS '日志类型：0-系统异常，1-业务操作';
COMMENT ON COLUMN public.operation_log.system_type    IS '系统类型：1-后台管理，0-前台用户';
COMMENT ON COLUMN public.operation_log.operation_time IS '操作时间';
COMMENT ON COLUMN public.operation_log.create_time    IS '创建时间';
COMMENT ON COLUMN public.operation_log.is_delete      IS '逻辑删除：0-未删除，1-已删除';
