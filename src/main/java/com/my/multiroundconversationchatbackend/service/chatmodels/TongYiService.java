package com.my.multiroundconversationchatbackend.service.chatmodels;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

/**
 * @author lihaixu
 * @date 2025年03月02日 17:40
 */
@Slf4j
@Service
public class TongYiService implements ChatModelService{

    @Override
    public String generateResponse(String systemPrompt, String context) {
        try {
            // 创建请求体
            RequestBody requestBody = new RequestBody(
                    //此处以qwen-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                    "qwen-plus",
                    new Message[] {
                            new Message("system", systemPrompt),
                            new Message("user", context)
                    }
            );

            // 将请求体转换为 JSON
            Gson gson = new Gson();
            String jsonInputString = gson.toJson(requestBody);

            // 创建 URL 对象
            URL url = new URL("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为 POST
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            httpURLConnection.setRequestProperty("Accept", "application/json");

            // 若没有配置环境变量，请用百炼API Key将下行替换为：String apiKey = "sk-xxx";
            String apiKey = "sk-d91d0975f46e427daf666aa2e5787779";
            String auth = "Bearer " + apiKey;
            httpURLConnection.setRequestProperty("Authorization", auth);

            // 启用输入输出流
            httpURLConnection.setDoOutput(true);

            // 写入请求体
            try (OutputStream os = httpURLConnection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应码
            int responseCode = httpURLConnection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // 读取响应体
            try (BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("Response Body: " + response);
                // 解析响应
                JSONObject responseJson = JSONUtil.parseObj(response);
                return responseJson.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "模型调用失败";
    }

    @Override
    public void generateStreamResponse(String systemPrompt, String context, PrintWriter writer) {

        try {
            // 创建请求体
            RequestBody requestBody = new RequestBody(
                    "qwen-plus",
                    new Message[]{
                            new Message("system", systemPrompt),
                            new Message("user", context)
                    },
                    true  // 设置stream为true
            );

            // 将请求体转换为 JSON
            Gson gson = new Gson();
            String jsonInputString = gson.toJson(requestBody);

            // 创建 URL 对象
            URL url = new URL("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            // 设置请求头
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + "sk-d91d0975f46e427daf666aa2e5787779");
            httpURLConnection.setDoOutput(true);

            // 写入请求体
            try (OutputStream os = httpURLConnection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 读取流式响应
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data)) {
                            writer.write("data: [DONE]\n\n");
                            writer.flush();
                            break;
                        }

                        JSONObject json = JSONUtil.parseObj(data);
                        String content = json.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("delta")
                                .getStr("content", "");

                        if (StrUtil.isNotEmpty(content)) {
                            writer.write("data: " + content + "\n\n");
                            writer.flush();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Stream generation error", e);
            writer.write("data: Error: " + e.getMessage() + "\n\n");
            writer.flush();
        }

    }

    static class Message {
        String role;
        String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    static class RequestBody {
        String model;
        Message[] messages;
        boolean stream;

        public RequestBody(String model, Message[] messages) {
            this.model = model;
            this.messages = messages;
        }

        public RequestBody(String model, Message[] messages, boolean stream) {
            this.model = model;
            this.messages = messages;
            this.stream = stream;
        }
    }

}