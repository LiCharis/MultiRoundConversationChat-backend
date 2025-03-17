package com.my.multiroundconversationchatbackend.service.chat.chatmodels;

import com.my.multiroundconversationchatbackend.model.dto.GenerateCharRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author lihaixu
 * @date 2025年03月02日 17:40
 */
@Slf4j
@Service
public class QwenChatService extends BaseChatModelService {

    @Value("${chat.qwen.model}")
    private String model;
    @Value("${chat.qwen.apiKey}")
    private String apiKey;
    @Value("${chat.qwen.url}")
    private String baseUrl;


    @Override
    public String doGenerate(String systemPrompt, String buildPrompt) {
        GenerateCharRequest generateCharRequest = new GenerateCharRequest();
        generateCharRequest.setModel(model);
        generateCharRequest.setUrl(baseUrl);
        generateCharRequest.setApiKey(apiKey);
        generateCharRequest.setSystemPrompt(systemPrompt);
        generateCharRequest.setBuildPrompt(buildPrompt);
        return generateResponse(generateCharRequest);
    }

}