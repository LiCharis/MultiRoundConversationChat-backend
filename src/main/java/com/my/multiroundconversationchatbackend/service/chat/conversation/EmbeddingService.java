package com.my.multiroundconversationchatbackend.service.chat.conversation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author lihaixu
 * @date 2025年03月22日 21:24
 * 嵌入向量服务 - 负责文本向量化和相似度计算
 */
@Service
@Slf4j
public class EmbeddingService {

    @Autowired
    private ModelClient modelClient;

    /**
     * 获取文本的嵌入向量
     */
    public float[] getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[384]; // 返回零向量
        }

        try {
            // 调用模型API获取嵌入
            return modelClient.createEmbedding(text);
        } catch (Exception e) {
            log.error("Error getting embedding: {}", e.getMessage(), e);
            // 返回零向量作为后备
            return new float[384];
        }
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
}

