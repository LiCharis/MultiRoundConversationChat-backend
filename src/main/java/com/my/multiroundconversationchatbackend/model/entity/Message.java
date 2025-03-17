package com.my.multiroundconversationchatbackend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message extends BaseModel {

    private String id;

    /**
     * 内容
     */
    private String content;

    /**
     * 角色
     */
    private String role;

    private String parentId;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 更新时间
     */
    private Date updateAt;


    private Meta meta;


    private Extra extra;



    static class Meta extends BaseModel{
        private String avatar;
        private String title;

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    static class Extra extends BaseModel {
        private String fromModel;

        public String getFromModel() {
            return fromModel;
        }

        public void setFromModel(String fromModel) {
            this.fromModel = fromModel;
        }
    }


    private static final long serialVersionUID = 1L;

}
