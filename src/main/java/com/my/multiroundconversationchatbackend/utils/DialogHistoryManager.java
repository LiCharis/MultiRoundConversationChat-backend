package com.my.multiroundconversationchatbackend.utils;

import com.my.multiroundconversationchatbackend.model.entity.DialogueRecord;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lihaixu
 * @date 2025年03月03日 11:32
 */
@Slf4j
public class DialogHistoryManager {
    // 默认对话历史记录上限
    public static final int MAX_HISTORY_SIZE = 10;
    private static final String HISTORY_KEY = "DIALOG_HISTORY";

    /**
     * 初始化对话历史
     */
    public static void init(HttpSession session) {
        session.setAttribute(HISTORY_KEY, new ArrayList<DialogueRecord>(MAX_HISTORY_SIZE));
    }

    /**
     * 获取会话中的对话历史
     */
    @SuppressWarnings("unchecked")
    private static List<DialogueRecord> getHistoryFromSession(HttpSession session) {
        List<DialogueRecord> history = (List<DialogueRecord>) session.getAttribute(HISTORY_KEY);
        if (history == null) {
            init(session);
            history = (List<DialogueRecord>) session.getAttribute(HISTORY_KEY);
        }
        return history;
    }

    /**
     * 添加对话记录
     */
    public static void addRecord(HttpSession session, DialogueRecord record) {
        List<DialogueRecord> history = getHistoryFromSession(session);

        // 如果超出上限，移除最早的记录
        if (history.size() >= MAX_HISTORY_SIZE) {
            history.remove(0);
        }

        history.add(record);
        session.setAttribute(HISTORY_KEY, history);
        log.debug("Added dialogue record for session {}: {}", session.getId(), record);
    }

    /**
     * 获取完整对话历史
     */
    public static List<DialogueRecord> getHistory(HttpSession session) {
        return getHistoryFromSession(session);
    }

    /**
     * 获取最近N轮对话历史
     */
    public static List<DialogueRecord> getRecentHistory(HttpSession session, int n) {
        List<DialogueRecord> history = getHistoryFromSession(session);
        if (history.isEmpty()) {
            return Collections.emptyList();
        }

        int start = Math.max(0, history.size() - n);
        return new ArrayList<>(history.subList(start, history.size()));
    }

    /**
     * 清空当前会话的对话历史
     */
    public static void clear(HttpSession session) {
        List<DialogueRecord> attribute = (List<DialogueRecord>) session.getAttribute(HISTORY_KEY);
        attribute.clear();
        log.debug("Cleared dialogue history for session {}", session.getId());
    }

    /**
     * 移除指定位置的对话记录
     */
    public static boolean removeRecord(HttpSession session, int index) {
        List<DialogueRecord> history = getHistoryFromSession(session);
        if (index < 0 || index >= history.size()) {
            return false;
        }

        history.remove(index);
        session.setAttribute(HISTORY_KEY, history);
        log.debug("Removed dialogue record at index {} for session {}", index, session.getId());
        return true;
    }

    /**
     * 获取对话历史大小
     */
    public static int size(HttpSession session) {
        return getHistoryFromSession(session).size();
    }

    /**
     * 检查对话历史是否为空
     */
    public static boolean isEmpty(HttpSession session) {
        return size(session) == 0;
    }

    /**
     * 检查对话是否已经达到容量上限
     */
    public static boolean isFull(HttpSession session) {
        return size(session) >= MAX_HISTORY_SIZE;
    }

    /**
     * 获取最后一条对话记录
     */
    public static DialogueRecord getLastRecord(HttpSession session) {
        List<DialogueRecord> history = getHistoryFromSession(session);
        if (history.isEmpty()) {
            return null;
        }
        return history.get(history.size() - 1);
    }

    /**
     * 批量添加对话记录
     */
    public static void addRecords(HttpSession session, List<DialogueRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        records.forEach(record -> addRecord(session, record));
        log.debug("Added {} dialogue records for session {}", records.size(), session.getId());
    }
}
