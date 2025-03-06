package com.my.multiroundconversationchatbackend.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document("chat")
public class MessageBody extends BaseModel{
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

    /**
     * 创建时间
     */
    @CreatedDate
    private Date createTime;

    /**
     * 最后修改时间
     */
    @LastModifiedDate
    private Date updateTime;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    private Integer isDeleted = 0;

}
