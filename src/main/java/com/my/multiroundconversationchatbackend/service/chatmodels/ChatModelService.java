package com.my.multiroundconversationchatbackend.service.chatmodels;

import java.io.PrintWriter;

/**
 * @author lihaixu
 * @date 2025年03月02日 17:41
 */
public interface ChatModelService {
    String generateResponse(String systemPrompt, String context);

    //流式输出
    void generateStreamResponse(String systemPrompt, String context, PrintWriter writer);
}
