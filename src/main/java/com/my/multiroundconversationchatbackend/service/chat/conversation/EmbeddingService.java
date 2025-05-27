package com.my.multiroundconversationchatbackend.service.chat.conversation;

import lombok.extern.slf4j.Slf4j;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * @author lihaixu
 * @date 2025年03月22日 21:24
 * 嵌入向量服务 - 负责文本向量化和相似度计算
 */
@Service
@Slf4j
public class EmbeddingService {

    @Value("${chat.qwen.apiKey}")
    private String apiKey;
    @Value("${chat.qwen.embeddingUrl}")
    private String baseUrl;
    /**
     * 获取文本的嵌入向量
     */
    public float[] getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[768]; // 返回零向量
        }

        // 构建请求体
        JSONObject requestBody = JSONUtil.createObj()
                .set("model", "text-embedding-v3")
                .set("input",text)
                .set("dimensions",768)
                .set("encoding_format","float");

        try {
            HttpResponse response = HttpRequest.post(baseUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();

            // 如果需要解析响应JSON
            JSONObject responseJson = JSONUtil.parseObj(response.body());
            log.info("解析后的JSON响应: {}", responseJson);

            String str = responseJson.getJSONArray("data")
                    .getJSONObject(0).getStr("embedding");
            log.info("解析后的embedding向量: {}", str);
            System.out.println(str);

            return normalizeVector(convertStringToFloatArray(str));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return new float[768];
    }

    /**
     * 计算向量的余弦相似度
     */
    public double calculateSimilarity(float[] vec1, float[] vec2) {
        if (vec1 == null || vec2 == null || vec1.length != vec2.length) {
            return 0.5; // 默认中等相似度
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        if (norm1 <= 0 || norm2 <= 0) {
            return 0.5; // 避免除零错误
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 归一化向量
     */
    private float[] normalizeVector(float[] vector) {
        double norm = 0.0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);

        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) (vector[i] / norm);
            }
        }
        return vector;
    }



    public static float[] convertStringToFloatArray(String str) {
        if (str == null || str.length() < 2) {
            // 处理空字符串或无效格式（至少需要 "[]"）
            return new float[0];
        }

        // 1. 去除方括号
        String content = str.substring(1, str.length() - 1);

        if (content.isEmpty()) {
            // 处理空数组情况，如 "[]"
            return new float[0];
        }

        // 2. 按逗号分割
        String[] numberStrings = content.split(",");

        // 3. 转换为浮点数并存入数组
        float[] floatArray = new float[numberStrings.length];
        for (int i = 0; i < numberStrings.length; i++) {
            try {
                // 去除可能存在的多余空格
                floatArray[i] = Float.parseFloat(numberStrings[i].trim());
            } catch (NumberFormatException e) {
                // 处理转换错误，例如如果字符串不是有效的浮点数
                System.err.println("Error parsing float from string: " + numberStrings[i]);
                // 可以选择抛出异常，或者返回null/空数组，或者给一个默认值
                // 这里我们简单地将错误值设为0，并打印错误信息
                floatArray[i] = 0.0f;
                // 或者更严格地处理: throw new IllegalArgumentException("Invalid number format in string: " + numberStrings[i], e);
            }
        }
        return floatArray;
    }



    public static void main(String[] args) {
        EmbeddingService embeddingService = new EmbeddingService();
        float[] embedding = embeddingService.getEmbedding("风急天高猿啸哀，渚清沙白鸟飞回，无边落木萧萧下，不尽长江滚滚来");
    }
}

