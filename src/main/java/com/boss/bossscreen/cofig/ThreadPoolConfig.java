package com.boss.bossscreen.cofig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/5
 */
@Configuration
public class ThreadPoolConfig {

    // 线程池核心线程数
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;
    // 线程池最大线程数
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    // 线程空闲时的存活时间
    private static final long KEEP_ALIVE_SECONDS = 60L;
    // 队列容量
    private static final int QUEUE_CAPACITY = 1000;
    // 工作队列类型：无界LinkedBlockingQueue，可根据需要替换为其他队列
    private static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    @Bean(name = "customThreadPool")
    public ThreadPoolExecutor customThreadPool() {
        return new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                WORK_QUEUE,
                new CustomThreadFactory(),
                new CustomRejectedExecutionHandler());
    }

    // 自定义线程工厂，可选，用于设置线程名称等
    private static class CustomThreadFactory implements ThreadFactory {
        private AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Custom-Pool-" + threadNumber.getAndIncrement());
            return thread;
        }
    }

    // 自定义拒绝策略，可选，用于处理任务过多时的行为
    private static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.println("Task " + r.toString() + " has been rejected.");
        }
    }
}
