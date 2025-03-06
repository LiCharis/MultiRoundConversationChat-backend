package com.my.multiroundconversationchatbackend.service.chatmodels;

import org.springframework.stereotype.Component;

import java.io.PrintWriter;

/**
 * @author lihaixu
 * @date 2025年03月05日 13:19
 */
@Component
public class MockChatService implements ChatModelService {
    @Override
    public String generateResponse(String systemPrompt, String buildPrompt) {

        return "mock response";
    }

    @Override
    public void generateStreamResponse(String systemPrompt, String context, PrintWriter writer) {

    }
}
