package com.my.multiroundconversationchatbackend.service.chat.chatmodels;
import com.my.multiroundconversationchatbackend.model.dto.GenerateCharRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author lihaixu
 * @date 2025年03月02日 17:07
 */
@Slf4j
@Service
public class DeepSeekChatService extends BaseChatModelService {

    @Value("${chat.deepseek.model}")
    private String model;
    @Value("${chat.deepseek.apiKey}")
    private String apiKey;
    @Value("${chat.deepseek.url}")
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

    @Data
    @AllArgsConstructor
    private static class Message {
        private String role;
        private String content;
    }


}

