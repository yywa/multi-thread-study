package com.yyw.study.pool;

/**
 * @author yyw
 * @date 2019/12/24
 */

public class InternalTask implements Runnable {

    private final RunnableQueue runnableQueue;
    private volatile boolean running = true;

    public InternalTask(RunnableQueue runnableQueue) {
        this.runnableQueue = runnableQueue;
    }

    @Override
    public void run() {
        //如果当前任务为running并且没有被中断，则其将不断地从queue中获取Runnable，然后执行run方法
        while (running && !Thread.currentThread().isInterrupted()) {
            Runnable task = runnableQueue.take();
            task.run();
        }
    }

    public void stop() {
        this.running = false;
    }
}
