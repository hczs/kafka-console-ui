package com.xuxd.kafka.console.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author houcheng
 * @version V1.0
 * @date 2022/6/16 23:45:22
 */
public class ExecutorServiceUtil {

    /**
     * 1. corePoolSize：线程池的核心线程数，也叫常驻线程数
     * 2. maximumPoolSize：能容纳的最大线程数
     * 3. keepAliveTime：空闲线程存活时间
     * 4. unit：存活时间的单位
     * 5. workQueue：存放提交但未执行任务的阻塞队列
     * 6. threadFactory：创建线程的工厂类
     * 7. handler：等待队列满之后的拒绝策略
     */
    public static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
            20,
            30,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());
}
