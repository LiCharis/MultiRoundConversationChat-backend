package com.my.multiroundconversationchatbackend.service.chat.conversation;


import com.my.multiroundconversationchatbackend.model.entity.DialogueContext;


/**
 * @author lihaixu
 * @date 2025年03月22日 21:06
 * 对话处理器接口
 */
public interface DialogueProcessor {
    /**
     * 处理对话上下文
     * @param context 对话上下文
     * @return 处理后的上下文
     */
    DialogueContext process(DialogueContext context);
}