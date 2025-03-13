package com.my.multiroundconversationchatbackend.model.dto;

import com.my.multiroundconversationchatbackend.model.entity.BaseModel;
import com.my.multiroundconversationchatbackend.model.entity.Message;
import lombok.Data;

/**
 * @author lihaixu
 * @date 2025年03月02日 15:37
 */
@Data
public class ChatRequest extends BaseModel {
    private Message messages;
    private String model;
}