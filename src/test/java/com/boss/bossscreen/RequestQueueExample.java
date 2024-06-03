package com.boss.bossscreen;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/5/28
 */
public class RequestQueueExample {
    // 假设的第三方服务URL
    private static final String THIRD_PARTY_URL = "http://example.com/api/data";

    // 创建一个阻塞队列用于存储请求
    private static final BlockingQueue<Map<String, String>> requestQueue = new LinkedBlockingQueue<>();

    // 创建线程池
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    // 模拟发送HTTP请求到第三方服务
    private static void sendRequest(Map<String, String> requestData) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(THIRD_PARTY_URL + "?" + requestData.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&")));

        try {
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            if (statusCode == 200) {
                System.out.println("Request succeeded with data: " + responseBody);
            } else {
                System.out.println("Request failed with status code: " + statusCode);

                // 如果状态码表示请求过多，可以实施退避策略
                if (statusCode == 429) {
                    try {
                        Thread.sleep(randomSleepTime()); // 随机等待一段时间后再重试
                        requestQueue.put(requestData); // 将请求重新放回队列
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成一个随机的等待时间（毫秒）
    private static long randomSleepTime() {
        return (long) (Math.random() * 5000); // 生成0到5秒之间的随机时间
    }

    // 工作线程，从队列中取出请求并发送
    private static class Worker implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Map<String, String> requestData = requestQueue.take(); // 阻塞，直到有请求可用
                    sendRequest(requestData);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // 如果线程被中断，则退出循环
                }
            }
        }
    }

    public static void main(String[] args) {
        // 提交工作线程到线程池
        for (int i = 0; i < 5; i++) {
            executorService.submit(new Worker());
        }

        // 发送请求到队列
        for (int i = 0; i < 100; i++) {
            Map<String, String> requestData = new HashMap<>();
            requestData.put("param", "value_" + i); // 模拟的请求数据
            try {
                requestQueue.put(requestData); // 将请求放入队列
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // 当所有请求都提交到队列后，关闭线程池（注意，这不会立即停止线程池中的线程）
        executorService.shutdown();

        // 等待线程池中的所有任务都执行完毕
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // 如果超时，则强制停止线程池
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow(); // 如果当前线程被中断，也强制停止线程池
        }

        System.out.println("All requests processed.");
    }
}
