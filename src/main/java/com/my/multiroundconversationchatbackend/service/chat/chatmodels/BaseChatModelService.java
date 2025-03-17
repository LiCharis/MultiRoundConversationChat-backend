package com.my.multiroundconversationchatbackend.service.chat.chatmodels;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.my.multiroundconversationchatbackend.model.dto.GenerateCharRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lihaixu
 * @date 2025年03月13日 15:36
 */
@Slf4j
public abstract class BaseChatModelService implements ChatModelService {


    public abstract String doGenerate(String systemPrompt, String buildPrompt);

    @Override
    public String generateResponse(GenerateCharRequest generateCharRequest) {
        String model = generateCharRequest.getModel();
        String url = generateCharRequest.getUrl();
        String apiKey = generateCharRequest.getApiKey();
        String systemPrompt = generateCharRequest.getSystemPrompt();
        String buildPrompt = generateCharRequest.getBuildPrompt();

        JSONArray messages = new JSONArray();
        JSONObject systemContent = new JSONObject();
        systemContent.set("role", "system");
        systemContent.set("content", systemPrompt);
        messages.add(systemContent);

        JSONObject userContent = new JSONObject();
        userContent.set("role", "user");
        userContent.set("content", buildPrompt);
        messages.add(userContent);


        // 构建请求体
        JSONObject requestBody = JSONUtil.createObj()
                .set("model", model)
                .set("messages", messages);

        try {
            // 发送请求
            HttpResponse response = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();

            // 处理响应
            log.info("响应状态码: {}", response.getStatus());
            log.info("响应内容: {}", response.body());

            // 如果需要解析响应JSON
            JSONObject responseJson = JSONUtil.parseObj(response.body());
            log.info("解析后的JSON响应: {}", responseJson);
            return responseJson.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");

        } catch (Exception e) {
            log.info("调用API出错: {}", e.getMessage());
            e.printStackTrace();
        }
        return "模型调用失败";
    }

    @Override
    public void generateStreamResponse(GenerateCharRequest generateCharRequest) {

    }
}
