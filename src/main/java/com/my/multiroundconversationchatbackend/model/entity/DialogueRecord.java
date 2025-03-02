package com.my.multiroundconversationchatbackend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author lihaixu
 * @date 2025年03月02日 16:47
 */
@Data
@AllArgsConstructor
public class DialogueRecord {
    private String content;
    private String role;  // user/system
    private long timestamp;
    private int turnIndex;
}