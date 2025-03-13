package com.my.multiroundconversationchatbackend.service.chatmodels;

import com.my.multiroundconversationchatbackend.model.dto.GenerateCharRequest;

import java.io.PrintWriter;

/**
 * @author lihaixu
 * @date 2025年03月02日 17:41
 */
public interface ChatModelService {
    String generateResponse(GenerateCharRequest generateCharRequest);

    //流式输出
    void generateStreamResponse(GenerateCharRequest generateCharRequest);
}
