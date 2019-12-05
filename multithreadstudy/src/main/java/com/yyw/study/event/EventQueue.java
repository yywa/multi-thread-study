package com.yyw.study.event;

import java.util.LinkedList;

/**
 * @author yyw
 * @date 2019/12/5
 */

public class EventQueue {
    private final int max;

    static class Event {

    }

    private final LinkedList<Event> eventQueue = new LinkedList<>();
    private final static int DEFAULT_MAX_EVENT = 10;

    public EventQueue() {
        this(DEFAULT_MAX_EVENT);
    }

    public EventQueue(int max) {
        this.max = max;
    }

    public void offer(Event event) {
        synchronized (eventQueue) {
            if (eventQueue.size() >= max) {
                try {
                    console("the queue is full");
                    eventQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            console("the new event is submitted");
            eventQueue.addLast(event);
            eventQueue.notify();
        }
    }

    public Event take() {
        synchronized (eventQueue) {
            if (eventQueue.isEmpty()) {
                try {
                    console("the queue is empty");
                    eventQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Event event = eventQueue.removeFirst();
            this.eventQueue.notify();
            console("the event" + event + "is handled");
            return event;
        }
    }

    public void console(String message) {
        System.out.printf("%s:%s\n", Thread.currentThread().getName(), message);
    }
}
