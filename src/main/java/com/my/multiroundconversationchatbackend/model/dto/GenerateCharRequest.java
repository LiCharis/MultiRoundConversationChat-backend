package com.my.multiroundconversationchatbackend.model.dto;

import lombok.Data;

/**
 * @author lihaixu
 * @date 2025年03月13日 15:39
 */
@Data
public class GenerateCharRequest {
    /**
     * 选用的模型
     */
    private String model;
    /**
     * 模型调用地址
     */
    private String url;
    /**
     * apiKey
     */
    private String apiKey;
    /**
     * 系统prompt
     */
    private String systemPrompt;
    /**
     * 构建好的用户问题
     */
    private String buildPrompt;
}
