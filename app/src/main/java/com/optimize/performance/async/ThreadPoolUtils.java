package com.optimize.performance.async;


import android.os.Process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolUtils {

    private final int CPUCOUNT = Runtime.getRuntime().availableProcessors();

    private final ThreadPoolExecutor cpuExecutor = new ThreadPoolExecutor(CPUCOUNT, CPUCOUNT,
            30, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), sThreadFactory);

    private final ThreadPoolExecutor iOExecutor = new ThreadPoolExecutor(64, 64,
            30, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), sThreadFactory);

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadPoolUtils #" + mCount.getAndIncrement());
        }
    };

    public static ExecutorService getService() {
        return sService;
    }

    private static final ExecutorService sService = Executors.newFixedThreadPool(5, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            // 创建线程命名
            Thread thread = new Thread(r, "ThreadPoolUtils");
            // 线程优先级设置
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            return thread;
        }
    });
}
