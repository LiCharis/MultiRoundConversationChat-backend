package com.my.multiroundconversationchatbackend.model.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author lihaixu
 * @date 2025年03月22日 22:47
 */
@Data
public class AddMessageBodyRequest {

    @NotNull(message = "添加id为null")
    private String id;

    @NotNull(message = "添加userId为null")
    private Long userId;

    /**
     * 标题
     */
    @NotNull(message = "添加title为null")
    private String title;

    /**
     * 消息
     */
    List<Message> messages;
}
