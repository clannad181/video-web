package com.cjym.videoweb.provider;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class CustomThreadPoolExecutorProvider<T> {

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(5,
            10, 60,
            TimeUnit.SECONDS,new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(),new ThreadPoolExecutor.DiscardOldestPolicy());

    private CustomThreadPoolExecutorProvider(){}

    public void execute(Runnable command){
        executor.execute(command);
    }

    public Future<T> submit(Callable<T> callable) {
        return executor.submit(callable);
    }
}
