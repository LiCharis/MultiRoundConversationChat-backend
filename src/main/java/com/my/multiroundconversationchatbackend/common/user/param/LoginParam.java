package com.my.multiroundconversationchatbackend.common.user.param;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lihaixu
 */
@Setter
@Getter
public class LoginParam extends RegisterParam {

    /**
     * 记住我
     */
    private Boolean rememberMe;
}
