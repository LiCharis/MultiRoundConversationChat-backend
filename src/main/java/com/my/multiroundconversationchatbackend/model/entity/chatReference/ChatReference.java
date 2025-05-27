package com.my.multiroundconversationchatbackend.model.entity.chatReference;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 聊天参数配置
 * @TableName chat_reference
 */
@TableName(value ="chat_reference")
@Data
public class ChatReference {

    /**
     *
     */
    private Long userId;

    /**
     * 
     */
    private Double timeWeight;

    /**
     * 
     */
    private Double frequencyWeight;

    /**
     * 
     */
    private Double semanticWeight;

    /**
     * 
     */
    private Double similarityWeight;

    /**
     * 
     */
    private Double timeFactorWeight;
}