package com.my.multiroundconversationchatbackend.service;


import com.my.multiroundconversationchatbackend.model.entity.MessageBody;

import java.util.List;

public interface ChatService {

    MessageBody getOneById(String id);

    MessageBody save(MessageBody messageBody);

    Boolean updateById(String id, String title);

    List<MessageBody> getListById(Long userId);

    Boolean deleteById(String id);
}
