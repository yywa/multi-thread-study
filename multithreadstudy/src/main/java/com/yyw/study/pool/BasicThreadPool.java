package com.yyw.study.pool;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yyw
 * @date 2019/12/24
 */

public class BasicThreadPool extends Thread implements ThreadPool {
    /**
     * 初始化线程数量
     */
    private final int initSize;
    /**
     * 线程池最大线程数
     */
    private final int maxSize;
    /**
     * 线程池核心线程数
     */
    private final int coreSize;
    /**
     * 当前活跃的线程数量
     */
    private int activeCount;
    /**
     * 创建线程的工厂
     */
    private final ThreadFactory threadFactory;
    /**
     * 任务队列
     */
    private final RunnableQueue runnableQueue;
    /**
     * 线程池是否被shutdown
     */
    private volatile boolean isShutdown = false;
    /**
     * 工作线程队列
     */
    private final Queue<ThreadTask> threadQueue = new ArrayDeque<>();

    private final static DenyPolicy DEFAULT_DENY_POLICY = new DenyPolicy.DiscardDenyPolicy();

    private final static ThreadFactory DEFAULT_THREAD_FACTORY = new DefaultThreadFactory();

    private final long keepAliveTime;

    private final TimeUnit timeUnit;

    public BasicThreadPool(int initSize, int maxSize, int coreSize, int queueSize) {
        this(initSize, maxSize, coreSize, DEFAULT_THREAD_FACTORY, queueSize, DEFAULT_DENY_POLICY, 10, TimeUnit.SECONDS);
    }


    public BasicThreadPool(int initSize, int maxSize, int coreSize, ThreadFactory threadFactory, int queueSize, DenyPolicy denyPolicy, long keepAliveTime, TimeUnit timeUnit) {
        this.initSize = initSize;
        this.maxSize = maxSize;
        this.coreSize = coreSize;
        this.threadFactory = threadFactory;
        this.runnableQueue = new LinkedRunnableQueue(queueSize, denyPolicy, this);
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.init();
    }

    public void init() {
        start();
        for (int i = 0; i < initSize; i++) {
            newThread();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        if (this.isShutdown) {
            throw new IllegalStateException("the thread pool is destroy");
        }
        this.runnableQueue.offer(runnable);
    }

    @Override
    public void shutdown() {
        synchronized (this) {
            if (isShutdown) {
                return;
            } else {
                isShutdown = true;
                threadQueue.forEach(threadTask -> {
                    threadTask.internalTask.stop();
                    threadTask.thread.interrupt();
                });
            }
            this.interrupt();
        }
    }

    @Override
    public int getInitSize() {
        if (this.isShutdown) {
            throw new IllegalStateException("the thread pool is destroy");
        }
        return this.initSize;
    }

    @Override
    public int getMaxSize() {
        if (this.isShutdown) {
            throw new IllegalStateException("the thread pool is destroy");
        }
        return this.maxSize;
    }

    @Override
    public int getCoreSize() {
        if (this.isShutdown) {
            throw new IllegalStateException("the thread pool is destroy");
        }
        return this.coreSize;
    }

    @Override
    public int getQueueSize() {
        if (this.isShutdown) {
            throw new IllegalStateException("the thread pool is destroy");
        }
        return runnableQueue.size();
    }

    @Override
    public int getActiveCount() {
        synchronized (this) {
            return this.activeCount;
        }
    }

    @Override
    public boolean isShutdown() {
        return this.isShutdown;
    }

    private void newThread() {
        InternalTask task = new InternalTask(runnableQueue);
        Thread thread = this.threadFactory.createThread(task);
        ThreadTask threadTask = new ThreadTask(thread, task);
        threadQueue.offer(threadTask);
        this.activeCount++;
        thread.start();
    }

    private void removeThread() {
        ThreadTask threadTask = threadQueue.remove();
        threadTask.internalTask.stop();
        this.activeCount--;
    }

    private static class ThreadTask {
        Thread thread;
        InternalTask internalTask;

        public ThreadTask(Thread thread, InternalTask internalTask) {
            this.thread = thread;
            this.internalTask = internalTask;
        }
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger GROUP_COUNTER = new AtomicInteger(1);

        private static final ThreadGroup group = new ThreadGroup("MyThreadPool-" + GROUP_COUNTER.getAndDecrement());

        private static final AtomicInteger COUNTER = new AtomicInteger(0);

        @Override
        public Thread createThread(Runnable runnable) {
            return new Thread(group, runnable, "thread-pool-" + COUNTER.getAndDecrement());
        }
    }


    @Override
    public void run() {
        while (!isShutdown && !isInterrupted()) {
            try {
                timeUnit.sleep(keepAliveTime);
            } catch (InterruptedException e) {
                isShutdown = true;
                break;
            }
            synchronized (this) {
                if (isShutdown) {
                    break;
                }
                //当前队列中有任务尚未处理，并且activeCount <coreSize 则继续扩容
                if (runnableQueue.size() > 0 && activeCount < coreSize) {
                    for (int i = initSize; i < coreSize; i++) {
                        newThread();
                    }
                    continue;
                }
                //当前队列中有任务尚未处理，并且activeCount<maxSize 则继续扩容
                if (runnableQueue.size() > 0 && activeCount < maxSize) {
                    newThread();
                    continue;
                }
                //如果任务队列没有任务，则需要回收，回收至coreSize即可。
                if (runnableQueue.size() == 0 && activeCount > coreSize) {
                    removeThread();
                }
            }
        }
    }
}

