package com.my.multiroundconversationchatbackend.model.dto;

import lombok.Data;

/**
 * @author lihaixu
 * @date 2025年03月02日 15:37
 */
@Data
public class ChatRequest {
    private Messages messages;
    private String model;

    @Data
    public static class Messages {
        private String content;
    }
}