package com.my.multiroundconversationchatbackend.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpSession;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lihaixu
 * @date 2025年03月06日 16:59
 */
@Slf4j
public class CounterManager {
    private static final String COUNTER_KEY = "SESSION_COUNTER";

    /**
     * 初始化会话计数器
     */
    public static void init(HttpSession session) {
        session.setAttribute(COUNTER_KEY, new AtomicInteger(0));
    }

    /**
     * 获取会话中的计数器
     */
    public static AtomicInteger getCounterFromSession(HttpSession session) {
        AtomicInteger counter = (AtomicInteger) session.getAttribute(COUNTER_KEY);
        if (counter == null) {
            init(session);
            counter = (AtomicInteger) session.getAttribute(COUNTER_KEY);
        }
        return counter;
    }

    /**
     * 获取当前计数
     */
    public static int getCurrentCount(HttpSession session) {
        return getCounterFromSession(session).get();
    }

    /**
     * 递增计数器并返回新值
     */
    public static int getAndIncrement(HttpSession session) {
        int value = getCounterFromSession(session).getAndIncrement();
        log.debug("Incremented counter for session {}: {}", session.getId(), value);
        return value;
    }

    /**
     * 重置计数器
     */
    public static void reset(HttpSession session) {
        getCounterFromSession(session).set(0);
        log.debug("Reset counter for session {}", session.getId());
    }

    /**
     * 清除计数器
     */
    public static void clear(HttpSession session) {
        session.removeAttribute(COUNTER_KEY);
        log.debug("Cleared counter for session {}", session.getId());
    }

    /**
     * 设置特定值
     */
    public static void setCount(HttpSession session, int value) {
        getCounterFromSession(session).set(value);
        log.debug("Set counter to {} for session {}", value, session.getId());
    }

    /**
     * 递减计数器并返回新值
     */
    public static int getAndDecrement(HttpSession session) {
        int value = getCounterFromSession(session).getAndDecrement();
        log.debug("Decremented counter for session {}: {}", session.getId(), value);
        return value;
    }

    /**
     * 检查计数器是否为零
     */
    public static boolean isZero(HttpSession session) {
        return getCurrentCount(session) == 0;
    }
}
