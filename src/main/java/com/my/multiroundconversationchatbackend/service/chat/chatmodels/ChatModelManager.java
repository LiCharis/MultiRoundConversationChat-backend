package com.my.multiroundconversationchatbackend.service.chat.chatmodels;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author lihaixu
 * @date 2025年03月02日 17:49
 */
@Component
public class ChatModelManager {
    @Autowired
    private Map<String,BaseChatModelService> chatModelServiceMap;

    public BaseChatModelService get(String modelName) {
        String beanName = splitNameToGetBean(modelName);
        BaseChatModelService chatModelService = chatModelServiceMap.get(beanName);
        if (chatModelService != null) {
            return chatModelService;
        } else {
            throw new UnsupportedOperationException(
                    "No ChatModelService Found With modelName : " + modelName);
        }
    }

    private String splitNameToGetBean(String modelName) {;
        String beanPrefix = "";
        if (modelName.contains("-")) {
           beanPrefix = modelName.split("-")[0];
        }else {
            beanPrefix = modelName;
        }
        return beanPrefix + "ChatService";
    }
}
