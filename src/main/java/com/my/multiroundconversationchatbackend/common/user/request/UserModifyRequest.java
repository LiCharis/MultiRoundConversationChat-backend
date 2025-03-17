package com.my.multiroundconversationchatbackend.common.user.request;

import lombok.*;

import javax.validation.constraints.NotNull;

/**
 * @author lihaixu
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserModifyRequest{
    @NotNull(message = "userId不能为空")
    private Long userId;
    private String nickName;
    private String password;
    private String profilePhotoUrl;
    private String telephone;

}
