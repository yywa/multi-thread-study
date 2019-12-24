package com.yyw.study.pool;

/**
 * @author yyw
 * @date 2019/12/24
 */

public interface RunnableQueue {
    /**
     * 当有新的任务进来首先会offer到队列中
     *
     * @param runnable
     */
    void offer(Runnable runnable);

    /**
     * 工作线程通过take方法来获取Runnable
     *
     * @return
     */
    Runnable take();

    /**
     * 获取任务队列中任务的数量
     *
     * @return
     */
    int size();
}
