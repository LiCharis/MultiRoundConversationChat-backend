package com.my.multiroundconversationchatbackend.service.chatmodels;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.core.map.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lihaixu
 * @date 2025年03月02日 17:07
 */
@Slf4j
@Service
public class DeepSeekService implements ChatModelService {
    private static final String API_KEY = "sk-fd63bcc9e83b47e9b5956ca1063e0a64";
    private static final String BASE_URL = "https://api.deepseek.com/chat/completions";

    @Data
    @AllArgsConstructor
    private static class Message {
        private String role;
        private String content;
    }

    public String generateResponse(String systemPrompt, String context) {
        try {
            // 构建消息列表
            List<Message> messages = new ArrayList<>();
            messages.add(new Message("system", systemPrompt));
            messages.add(new Message("user", context));

            // 构建请求体
            Map<Object, Object> requestBody = MapUtil.builder()
                    .put("model", "deepseek-chat")
                    .put("messages", messages)
                    .put("stream", false)
                    .build();

            log.info(JSONUtil.toJsonStr(requestBody));

            // 发送请求
            HttpResponse response = HttpRequest.post(BASE_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(requestBody))
                    .timeout(30000) // 30秒超时
                    .execute();

            // 检查响应状态
            if (!response.isOk()) {
                throw new RuntimeException("API request failed with status: " + response.getStatus());
            }

            // 解析响应
            JSONObject responseJson = JSONUtil.parseObj(response.body());
            return responseJson.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");

        } catch (Exception e) {
            log.error("Error calling DeepSeek API", e);
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public void generateStreamResponse(String systemPrompt, String context, PrintWriter writer) {

    }
}

