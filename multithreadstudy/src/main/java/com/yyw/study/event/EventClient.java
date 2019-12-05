package com.yyw.study.event;

import java.util.concurrent.TimeUnit;

/**
 * @author yyw
 * @date 2019/12/5
 */

public class EventClient {
    public static void main(String[] args) {
        final EventQueue eventQueue = new EventQueue();
        new Thread(() -> {
            for (; ; ) {
                eventQueue.offer(new EventQueue.Event());
            }
        }, "Producer").start();

        new Thread(() -> {
            for (; ; ) {
                eventQueue.take();
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "consumer").start();
    }
}
