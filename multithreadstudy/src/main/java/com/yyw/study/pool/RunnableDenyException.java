package com.yyw.study.pool;

/**
 * @author yyw
 * @date 2019/12/24
 */

public class RunnableDenyException extends RuntimeException {
    public RunnableDenyException(String message) {
        super(message);
    }
}
