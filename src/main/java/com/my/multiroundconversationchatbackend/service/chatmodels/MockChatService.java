package com.my.multiroundconversationchatbackend.service.chatmodels;

import com.my.multiroundconversationchatbackend.model.dto.GenerateCharRequest;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

/**
 * @author lihaixu
 * @date 2025年03月05日 13:19
 */
@Component
public class MockChatService implements ChatModelService {
    @Override
    public String generateResponse(GenerateCharRequest generateCharRequest) {

        return String.format("model: {}, res: mock response", generateCharRequest.getModel());
    }

    @Override
    public void generateStreamResponse(GenerateCharRequest generateCharRequest) {

    }
}
