package com.cjym.videoweb.handler.exception;


import com.cjym.videoweb.exception.GlobeException;
import com.cjym.videoweb.result.R;
import lombok.extern.slf4j.Slf4j;
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

    @ExceptionHandler({
            Exception.class,
            AsyncRequestTimeoutException.class,
            ConnectException.class,
            NumberFormatException.class
    })
    public R<String> otherException(Exception e){
        log.error("发生错误");
        log.error(e.getMessage());
        e.printStackTrace();
        return R.error(e.getMessage());
    }
}
