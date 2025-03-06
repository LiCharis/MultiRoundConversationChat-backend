package com.my.multiroundconversationchatbackend.service.chatmodels;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author lihaixu
 * @date 2025年03月02日 17:49
 */
@Component
public class ChatModelManager {
    @Autowired
    private Map<String,ChatModelService> chatModelServiceMap;


    @Value("${spring.profiles.active}")
    private String profile;

    public ChatModelService get(String modelName) {

        if ("dev".equals(profile)) {
            return chatModelServiceMap.get("mockChatService");
        }
        //组装出beanName，并从map中获取对应的bean
        ChatModelService chatModelService = chatModelServiceMap.get(modelName);

        if (chatModelService != null) {
            return chatModelService;
        } else {
            throw new UnsupportedOperationException(
                    "No ChatModelService Found With modelName : " + modelName);
        }
    }
}
