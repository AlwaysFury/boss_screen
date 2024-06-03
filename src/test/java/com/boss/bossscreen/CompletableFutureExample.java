package com.boss.bossscreen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/5/28
 */
public class CompletableFutureExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 创建一个存储CompletableFuture对象的列表
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 提交多个异步任务到futures列表
        for (int i = 0; i < 10; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // 模拟长时间运行的任务
                try {
                    Thread.sleep((long) (Math.random() * 1000));
                    System.out.println("Task " + Thread.currentThread().getName() + " finished.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            });
            futures.add(future);
        }

        // 使用CompletableFuture.allOf等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // 等待所有任务完成，并处理异常（如果有的话）
        allFutures.join(); // join方法会阻塞直到所有任务完成，如果任何任务抛出异常，这里会抛出CompletionException

        // 在这里添加后续的业务逻辑
        System.out.println("All tasks have finished. Executing post-processing logic...");

        // 如果你想检查每个Future的结果（对于Void类型的Future，通常没有结果需要检查）
        // 但你可以遍历futures列表并调用get()方法（尽管对于Void类型的Future，get()会返回null）
        for (CompletableFuture<Void> future : futures) {
            try {
                future.get(); // 如果任务中抛出了异常，这里会抛出ExecutionException
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }
}
