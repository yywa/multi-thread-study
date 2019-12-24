package com.yyw.study.pool;

/**
 * @author yyw
 * @date 2019/12/24
 */

@FunctionalInterface
public interface ThreadFactory {
    Thread createThread(Runnable runnable);
}
