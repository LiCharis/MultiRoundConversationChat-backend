package com.my.multiroundconversationchatbackend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * @author lihaixu
 * @date 2025年03月02日 16:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DialogueRecord extends BaseModel{
    private String query;
    private String response;
    private long timestamp;
    private int turnIndex;
    private double weight;
    private SemanticFeature semanticFeature;

    /**
     * 语义特征类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SemanticFeature extends BaseModel{
        private Set<String> keywords;        // 关键词集合
        private double frequency;            // 出现频率
        private Map<String, Double> topics;  // 主题分布
    }
}