package com.my.multiroundconversationchatbackend.service.chatReference.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.my.multiroundconversationchatbackend.model.entity.chatReference.ChatReference;
import com.my.multiroundconversationchatbackend.service.chatReference.service.ChatReferenceService;
import com.my.multiroundconversationchatbackend.mapper.ChatReferenceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
* @author lihaixu
* @description 针对表【chat_reference(聊天参数配置)】的数据库操作Service实现
* @createDate 2025-05-25 23:09:00
*/
@Service
public class ChatReferenceServiceImpl extends ServiceImpl<ChatReferenceMapper, ChatReference>
    implements ChatReferenceService{

    @Autowired
    private ChatReferenceMapper chatReferenceMapper;

    public int updateChatReference(ChatReference chatReference) {
        UpdateWrapper<ChatReference> chatReferenceUpdateWrapper = new UpdateWrapper<>();
        chatReferenceUpdateWrapper.eq("user_id", chatReference.getUserId());
        return chatReferenceMapper.update(chatReference,chatReferenceUpdateWrapper);
    }

    @Override
    public ChatReference findChatReferenceByUserId(Long userId) {
        QueryWrapper<ChatReference> chatReferenceQueryWrapper = new QueryWrapper<>();
        chatReferenceQueryWrapper.eq("user_id", userId);
        return chatReferenceMapper.selectOne(chatReferenceQueryWrapper);
    }
}




