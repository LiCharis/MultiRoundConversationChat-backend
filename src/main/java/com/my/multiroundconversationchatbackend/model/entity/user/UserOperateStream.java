package com.my.multiroundconversationchatbackend.model.entity.user;

import com.my.multiroundconversationchatbackend.model.entity.BaseEntity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author lihaixu
 * @date 2025年03月15日 20:43
 */
@Getter
@Setter
public class UserOperateStream extends BaseEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 操作类型
     */
    private String type;

    /**
     * 操作时间
     */
    private Date operateTime;

    /**
     * 操作参数
     */
    private String param;

    /**
     * 扩展字段
     */
    private String extendInfo;

}
