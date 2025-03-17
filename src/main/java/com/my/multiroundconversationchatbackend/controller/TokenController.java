package com.my.multiroundconversationchatbackend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.my.multiroundconversationchatbackend.common.BaseResponse;
import com.my.multiroundconversationchatbackend.common.ErrorCode;
import com.my.multiroundconversationchatbackend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.my.multiroundconversationchatbackend.constant.CacheConstant.CACHE_KEY_SEPARATOR;

/**
 * @author lihaixu
 * @date 2025年03月15日 19:33
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("token")
public class TokenController {

    private static final String TOKEN_PREFIX = "token:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/get")
    public BaseResponse<String> get(@NotBlank String scene) {
        if (StpUtil.isLogin()) {
            String token = UUID.randomUUID().toString();
            String tokenKey = TOKEN_PREFIX + scene + CACHE_KEY_SEPARATOR + token;
            stringRedisTemplate.opsForValue().set(tokenKey, token, 30, TimeUnit.MINUTES);
            return BaseResponse.success(tokenKey);
        }
        throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
    }
}
