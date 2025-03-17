package com.my.multiroundconversationchatbackend.common.user.request.condition;

import lombok.*;

/**
 * @author lihaixu
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserPhoneQueryCondition implements UserQueryCondition {

    private static final long serialVersionUID = 1L;

    /**
     * 用户手机号
     */
    private String telephone;
}
