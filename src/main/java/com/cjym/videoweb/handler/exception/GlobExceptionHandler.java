package com.cjym.videoweb.handler.exception;


import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.enums.ResultEnum;
import com.cjym.videoweb.result.R;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import java.net.ConnectException;

/**
 * 统一异常处理
 * */
@RestControllerAdvice
@Slf4j
@Component
public class GlobExceptionHandler {
    @ExceptionHandler(GlobeException.class)
    public R<String> exceptionHandler(GlobeException e){
        log.error(e.getMessage(),e);
        return R.error(e.getMessage());
    }

    /**
     * token 过期：拦截器放行路径（如 /client/user/**）下 Service 内自行解析 JWT 时会抛出，
     * 属于预期业务场景，返回友好提示而非 ERROR 堆栈，前端据此刷新登录
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public R<String> expiredJwtExceptionHandler(ExpiredJwtException e){
        log.warn("token 已过期: {}", e.getMessage());
        return R.error("登录已过期，请重新登录");
    }

    /**
     * token 非法（签名错误/格式错误等）：返回统一提示
     */
    @ExceptionHandler(JwtException.class)
    public R<String> jwtExceptionHandler(JwtException e){
        log.warn("token 校验失败: {}", e.getMessage());
        return R.error(ResultEnum.TOKEN_ERROR.getMessage());
    }

    @ExceptionHandler({
            Exception.class,
            AsyncRequestTimeoutException.class,
            ConnectException.class,
            NumberFormatException.class,
            DataAccessException.class
    })
    public R<String> otherException(Exception e){
        log.error("发生错误");
        log.error(e.getMessage());
        e.printStackTrace();
        return R.error(e.getMessage());
    }
}
