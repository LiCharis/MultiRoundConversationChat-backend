package com.my.multiroundconversationchatbackend.service.chat.chatmodels;
import org.springframework.stereotype.Component;

/**
 * @author lihaixu
 * @date 2025年03月05日 13:19
 */
@Component
public class MockChatService extends BaseChatModelService {

    @Override
    public String doGenerate(String systemPrompt, String buildPrompt) {
        return String.format("systemPrompt: %s buildPrompt: %s response: %s", systemPrompt, buildPrompt,"mock response");
    }
}
