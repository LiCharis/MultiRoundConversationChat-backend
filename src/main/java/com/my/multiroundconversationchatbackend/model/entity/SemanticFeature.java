package com.my.multiroundconversationchatbackend.model.entity;

import com.my.multiroundconversationchatbackend.service.chat.conversation.EmbeddingService;
import com.my.multiroundconversationchatbackend.utils.SemanticUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lihaixu
 * @date 2025年03月22日 20:59
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticFeature {
    private Set<String> keywords;
    private Map<String, Double> topics;
    private double frequency;
    private float[] embedding;
    private List<String> entities;
    private double coherenceScore;

    // 静态构建器
    public static SemanticFeature fromContent(
            String content,
            EmbeddingService embeddingService) {


        return SemanticFeature.builder()
                .keywords(SemanticUtils.extractKeywords(content))
                .topics(SemanticUtils.calculateTopicDistribution(content))
                .frequency(SemanticUtils.calculateTermFrequency(content))
                .embedding(embeddingService.getEmbedding(content))
                .entities(SemanticUtils.extractEntities(content))
                .coherenceScore(SemanticUtils.calculateCoherence(
                        SemanticUtils.calculateTopicDistribution(content)))
                .build();
    }
}