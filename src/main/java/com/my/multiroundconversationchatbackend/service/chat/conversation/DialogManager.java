package com.my.multiroundconversationchatbackend.service.chat.conversation;


import com.my.multiroundconversationchatbackend.model.entity.DialogueContext;
import com.my.multiroundconversationchatbackend.model.entity.DialogueRecord;
import com.my.multiroundconversationchatbackend.model.entity.SemanticFeature;
import com.my.multiroundconversationchatbackend.utils.DialogHistoryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpSession;
import com.my.multiroundconversationchatbackend.utils.CounterManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lihaixu
 * @date 2025年03月22日 21:07
 * 对话管理器 - 负责管理对话历史和窗口
 */
@Order(1)
@Service
@Slf4j
public class DialogManager implements DialogueProcessor {

    @Override
    public DialogueContext process(DialogueContext context) {
        HttpSession session = context.getSession();

        // 初始化对话历史
        if (DialogHistoryManager.getHistory(session).isEmpty()) {
            log.info("Starting new conversation");
            CounterManager.init(session);
        } else {
            // 管理对话窗口
            manageDialogueWindow(session);
        }

        // 设置历史记录到上下文
        context.setHistory(DialogHistoryManager.getHistory(session));

        return context;
    }

    /**
     * 管理对话窗口大小
     */
    private void manageDialogueWindow(HttpSession session) {
        while (DialogHistoryManager.isFull(session)) {
            List<DialogueRecord> history = DialogHistoryManager.getHistory(session);
            log.debug("Removing oldest dialogue record: {}", history.get(0).getTurnIndex());
            history.remove(0);
        }
    }

    /**
     * 更新对话历史
     */
    public void updateHistory(DialogueContext context) {
        String query = context.getQuery();
        String response = context.getResponse();
        HttpSession session = context.getSession();

        SemanticFeature feature = context.getAttribute("semanticFeature");
        AtomicInteger counter = CounterManager.getCounterFromSession(session);

        DialogueRecord record = new DialogueRecord(
                query,
                response,
                System.currentTimeMillis(),
                counter.getAndIncrement(),
                0, // 初始权重
                feature
        );

        DialogHistoryManager.addRecord(session, record);

        log.debug("Updated dialogue history, size: {}", DialogHistoryManager.size(session));
    }
}
