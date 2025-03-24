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
}