package com.my.multiroundconversationchatbackend.utils;

import com.my.multiroundconversationchatbackend.model.entity.DialogueRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lihaixu
 * @date 2025年03月03日 11:32
 */
@Slf4j
public class DialogHistoryThreadLocal {
    private static final ThreadLocal<List<DialogueRecord>> threadLocal = new ThreadLocal<>();

    // 默认对话历史记录上限
    private static final int MAX_HISTORY_SIZE = 5;

    /**
     * 初始化对话历史
     */
    public static void init() {
        threadLocal.set(new ArrayList<>(MAX_HISTORY_SIZE));
    }

    /**
     * 添加对话记录
     *
     * @param record 对话记录
     */
    public static void addRecord(DialogueRecord record) {
        List<DialogueRecord> history = getHistory();
        if (history == null) {
            init();
            history = getHistory();
        }

        // 如果超出上限，移除最早的记录
        if (history.size() >= MAX_HISTORY_SIZE) {
            history.remove(0);
        }

        history.add(record);
        log.debug("Added dialogue record: {}", record);
    }

    /**
     * 获取完整对话历史
     *
     * @return 对话历史列表
     */
    public static List<DialogueRecord> getHistory() {
        // 初始化或获取对话历史
        if (threadLocal.get() == null) {
            init();
        }
        return threadLocal.get();
    }

    /**
     * 获取最近N轮对话历史
     *
     * @param n 获取轮数
     * @return 最近n轮对话历史
     */
    public static List<DialogueRecord> getRecentHistory(int n) {
        List<DialogueRecord> history = getHistory();
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }

        int start = Math.max(0, history.size() - n);
        return new ArrayList<>(history.subList(start, history.size()));
    }

    /**
     * 清空当前线程的对话历史
     */
    public static void clear() {
        List<DialogueRecord> history = threadLocal.get();
        if (history != null) {
            history.clear();
        }
        threadLocal.remove();
        log.debug("Cleared dialogue history");
    }

    /**
     * 移除指定位置的对话记录
     *
     * @param index 要移除的记录索引
     * @return 是否移除成功
     */
    public static boolean removeRecord(int index) {
        List<DialogueRecord> history = getHistory();
        if (history == null || index < 0 || index >= history.size()) {
            return false;
        }

        history.remove(index);
        log.debug("Removed dialogue record at index: {}", index);
        return true;
    }

    /**
     * 获取对话历史大小
     *
     * @return 历史记录数量
     */
    public static int size() {
        List<DialogueRecord> history = getHistory();
        return history.size();
    }

    /**
     * 检查对话历史是否为空
     *
     * @return 是否为空
     */
    public static boolean isEmpty() {
        return size() == 0;
    }
    /**
     * 检查对话是否已经达到容量上限
     */
    public static boolean isFull(){
        return size() >= MAX_HISTORY_SIZE;
    }

    /**
     * 获取最后一条对话记录
     *
     * @return 最后一条对话记录，如果历史为空返回null
     */
    public static DialogueRecord getLastRecord() {
        List<DialogueRecord> history = getHistory();
        if (history == null || history.isEmpty()) {
            return null;
        }
        return history.get(history.size() - 1);
    }

    /**
     * 批量添加对话记录
     *
     * @param records 要添加的对话记录列表
     */
    public static void addRecords(List<DialogueRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        records.forEach(DialogHistoryThreadLocal::addRecord);
        log.debug("Added {} dialogue records", records.size());
    }
}
