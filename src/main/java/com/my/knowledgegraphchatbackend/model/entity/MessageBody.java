package com.my.knowledgegraphchatbackend.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document("chat")
public class MessageBody {
    public static final String COLLECTION_NAME = "chart";
    /**
     * MongoDB自动生成的唯一ID
     */
    @Id
    private String id;

    /**
     * 用户ID
     */
    @Indexed
    @JsonSerialize(using= ToStringSerializer.class)
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 消息
     */
    List<Message> messages;

}
