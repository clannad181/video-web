package com.cjym.videoweb.aspect;

import com.cjym.videoweb.constant.CaptchaConstant;
import com.cjym.videoweb.model.entity.LoginLog;
import com.cjym.videoweb.model.vo.UserLoginVo;
import com.cjym.videoweb.service.LoginLogService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 登录日志 AOP 切面
 * <p>
 * 拦截 UserInfoServiceImpl.login() 方法，自动记录每次登录的：
 * - 登录账号（用户名/邮箱）
 * - 登录方式（图片验证码/邮箱验证码）
 * - 登录IP、浏览器UA
 * - 登录结果（成功/失败及失败原因）
 */
@Aspect
@Component
public class LoginLogAspect {

    @Resource
    private LoginLogService loginLogService;

    /**
     * 切点：直接匹配 UserInfoServiceImpl.login() 方法
     * 不依赖注解，无需修改现有代码
     */
    @Pointcut("execution(* com.cjym.videoweb.service.impl.UserInfoServiceImpl.login(..))")
    public void loginPointcut() {
    }

    /**
     * 环绕通知：在 login 方法执行前后记录日志
     */
    @Around("loginPointcut()")
    public Object aroundLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 从方法参数中提取登录信息
        UserLoginVo loginVo = extractLoginVo(joinPoint);
        // 2. 构建 LoginLog 基础对象
        LoginLog loginLog = buildBaseLoginLog(loginVo);

        try {
            // 3. 执行原始登录方法
            Object result = joinPoint.proceed();
            // 4. 登录成功 → 记录成功日志
            loginLogService.recordLoginSuccess(loginLog);
            return result;
        } catch (Throwable e) {
            // 5. 登录失败 → 记录失败原因并记录日志
            loginLog.setFailReason(extractFailReason(e));
            loginLogService.recordLoginFailure(loginLog);
            // 6. 原样抛出异常，不改变原始业务逻辑
            throw e;
        }
    }

    /**
     * 从切点参数中提取 UserLoginVo
     */
    private UserLoginVo extractLoginVo(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof UserLoginVo) {
                return (UserLoginVo) arg;
            }
        }
        return null;
    }

    /**
     * 构建 LoginLog 基础对象（IP、UA、账号、登录方式等）
     */
    private LoginLog buildBaseLoginLog(UserLoginVo loginVo) {
        LoginLog loginLog = new LoginLog();

        if (loginVo != null) {
            // 根据登录类型确定账号字段
            if (CaptchaConstant.ADMIN_IMG_CAPTCHA.equals(loginVo.getLoginType())) {
                loginLog.setAccount(loginVo.getUsername());
                loginLog.setLoginType(CaptchaConstant.ADMIN_IMG_CAPTCHA);
            } else {
                loginLog.setAccount(loginVo.getEmail());
                loginLog.setLoginType(CaptchaConstant.ADMIN_EMAIL_CAPTCHA);
            }
        }

        // 填充请求上下文信息
        fillRequestInfo(loginLog);
        return loginLog;
    }

    /**
     * 从 RequestContextHolder 获取当前请求的 IP、UserAgent 和 systemType
     */
    private void fillRequestInfo(LoginLog loginLog) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                loginLog.setIpAddress(getClientIp(request));
                loginLog.setUserAgent(request.getHeader("User-Agent"));
                loginLog.setSystemType(resolveSystemType(request));
            }
        } catch (Exception ignored) {
            // 非 Web 上下文时忽略
        }
    }

    /**
     * 根据请求路径判断系统类型
     * 1-后台管理（/admin/），0-前台用户
     */
    private Integer resolveSystemType(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("/admin/")) {
            return 0;
        }
        return 1;
    }

    /**
     * 获取客户端真实 IP（考虑代理场景）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 提取异常中的失败原因
     */
    private String extractFailReason(Throwable e) {
        if (e == null) {
            return "未知错误";
        }
        String message = e.getMessage();
        if (StringUtils.isEmpty(message)) {
            return e.getClass().getSimpleName();
        }
        // 截断过长的错误信息，避免数据库字段溢出
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
