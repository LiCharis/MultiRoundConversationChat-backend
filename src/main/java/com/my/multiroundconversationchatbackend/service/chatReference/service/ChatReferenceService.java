package com.my.multiroundconversationchatbackend.service.chatReference.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.my.multiroundconversationchatbackend.model.entity.chatReference.ChatReference;

/**
* @author lihaixu
* @description 针对表【chat_reference(聊天参数配置)】的数据库操作Service
* @createDate 2025-05-25 23:09:00
*/
public interface ChatReferenceService extends IService<ChatReference> {
    int updateChatReference(ChatReference chatReference);
    ChatReference findChatReferenceByUserId(Long userId);
}
