package com.my.multiroundconversationchatbackend.service.chat.conversation;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author lihaixu
 * @date 2025年03月22日 21:25
 * 模型客户端 - 封装底层模型API调用
 */
@Component
@Slf4j
public class ModelClient {

    /**
     * 创建文本嵌入向量
     */
    public float[] createEmbedding(String text) {
        // todo 实际实现应调用嵌入模型API
        // 这里返回模拟的向量数据
        float[] embedding = new float[384];
        for (int i = 0; i < embedding.length; i++) {
            // 使用文本哈希确保同样的文本得到同样的向量
            embedding[i] = (float) Math.sin(text.hashCode() * 0.1 + i * 0.01);
        }
        return normalizeVector(embedding);
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
}
