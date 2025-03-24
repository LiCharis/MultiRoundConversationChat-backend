package com.my.multiroundconversationchatbackend.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChatUpdateRequest {
    @NotNull(message = "更新id为null")
    private String id;
    /**
     * 标题
     */
    @NotNull(message = "更新title为null")
    private String title;
}
