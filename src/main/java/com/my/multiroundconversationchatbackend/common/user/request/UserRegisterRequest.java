package com.my.multiroundconversationchatbackend.common.user.request;
import lombok.*;

/**
 * @author lihaixu
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest{

    private String telephone;

    private String inviteCode;

    private String password;

}
