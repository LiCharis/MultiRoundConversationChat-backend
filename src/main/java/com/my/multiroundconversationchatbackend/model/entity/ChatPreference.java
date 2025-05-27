package com.my.multiroundconversationchatbackend.model.entity;

import lombok.Data;

/**
 * @author lihaixu
 * @date 2025年05月25日 22:42
 */
@Data
public class ChatPreference {
    private Long userId;
    // 权重配置
    private double timeWeight;
    private double frequencyWeight;
    private double semanticWeight;

    // 语义配置
    private double similarityWeight;
    private double timeFactorWeight;
}
