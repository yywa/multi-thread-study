package com.yyw.study.pool;

import java.util.concurrent.TimeUnit;

/**
 * @author yyw
 * @date 2019/12/24
 */

public class ThreadPoolTest {

    public static void main(String[] args) throws InterruptedException {
        final ThreadPool threadPool = new BasicThreadPool(2, 6, 4, 1000);
        for (int i = 0; i < 20; i++) {
            threadPool.execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    System.out.println(Thread.currentThread().getName() + " is Running and done");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        for (; ; ) {
            System.out.println("getActiveCount: " + threadPool.getActiveCount());
            System.out.println("getQueueSize: " + threadPool.getQueueSize());
            System.out.println("getCoreSize: " + threadPool.getQueueSize());
            System.out.println("getMaxSize: " + threadPool.getMaxSize());
            System.out.println("========================================");
            TimeUnit.SECONDS.sleep(5);
        }
    }
}
