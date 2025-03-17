package com.my.multiroundconversationchatbackend.controller;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.my.multiroundconversationchatbackend.annotation.IsMobile;
import com.my.multiroundconversationchatbackend.common.BaseResponse;
import com.my.multiroundconversationchatbackend.common.ErrorCode;
import com.my.multiroundconversationchatbackend.common.user.param.LoginParam;
import com.my.multiroundconversationchatbackend.common.user.param.RegisterParam;
import com.my.multiroundconversationchatbackend.common.user.vo.LoginVO;
import com.my.multiroundconversationchatbackend.exception.BusinessException;
import com.my.multiroundconversationchatbackend.model.entity.user.User;
import com.my.multiroundconversationchatbackend.service.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

import static com.my.multiroundconversationchatbackend.constant.NoticeConstant.CAPTCHA_KEY_PREFIX;

/**
 * @author lihaixu
 * @date 2025年03月15日 19:32
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    private static final String ROOT_CAPTCHA = "8888";

    /**
     * 默认登录超时时间：7天
     */
    private static final Integer DEFAULT_LOGIN_SESSION_TIMEOUT = 60 * 60 * 24 * 7;


    @GetMapping("/sendCaptcha")
    public BaseResponse<Boolean> sendCaptcha(@IsMobile String telephone) {
        // 生成验证码
//        String captcha = RandomUtil.randomNumbers(4);
        String captcha = "8888";

        // 验证码存入Redis
        redisTemplate.opsForValue().set(CAPTCHA_KEY_PREFIX + telephone, captcha, 5, TimeUnit.MINUTES);

        //todo 发送验证码的逻辑

        return BaseResponse.success(true);
    }

    @PostMapping("/register")
    public BaseResponse<Boolean> register(@Valid @RequestBody RegisterParam registerParam) {

        //验证码校验
        String cachedCode = redisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + registerParam.getTelephone());
        if (!StringUtils.equalsIgnoreCase(cachedCode, registerParam.getCaptcha())) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_WRONG);
        }
        //注册
        return userService.register(registerParam.getTelephone());
    }

    /**
     * 登录方法
     *
     * @param loginParam 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public BaseResponse<LoginVO> login(@Valid @RequestBody LoginParam loginParam) {
        //fixme 为了方便，暂时直接跳过
        String telephone = loginParam.getTelephone();
        if (!ROOT_CAPTCHA.equals(loginParam.getCaptcha())) {
            //验证码校验
            String cachedCode = redisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + telephone);
            if (!StringUtils.equalsIgnoreCase(cachedCode, loginParam.getCaptcha())) {
                throw new BusinessException(ErrorCode.VERIFICATION_CODE_WRONG);
            }
        }
        //判断是注册还是登陆
        //查询用户信息
        User user = userService.findByTelephone(telephone);
        if (user == null) {
            //需要注册
            BaseResponse<Boolean> result = userService.register(telephone);
            if (result.getData()) {
                user = userService.findByTelephone(telephone);
                StpUtil.login(user.getId(), new SaLoginModel().setIsLastingCookie(loginParam.getRememberMe())
                        .setTimeout(DEFAULT_LOGIN_SESSION_TIMEOUT));
                StpUtil.getSession().set(user.getId().toString(), user);
                LoginVO loginVO = new LoginVO(user);
                return BaseResponse.success(loginVO);
            }
            return BaseResponse.error(500, "登陆失败");
        } else {
            //登录
            StpUtil.login(user.getId(), new SaLoginModel().setIsLastingCookie(loginParam.getRememberMe())
                    .setTimeout(DEFAULT_LOGIN_SESSION_TIMEOUT));
            StpUtil.getSession().set(user.getId().toString(), user);
            LoginVO loginVO = new LoginVO(user);
            return BaseResponse.success(loginVO);
        }
    }

    @RequestMapping("/getProfile")
    public BaseResponse<User> getProfile() {
        if (StpUtil.isLogin()) {
            return BaseResponse.success(userService.findById(Long.valueOf((String) StpUtil.getLoginId())));
        }
        return new BaseResponse<>(ErrorCode.USER_NOT_LOGIN);
    }

    @RequestMapping("/checkLogin")
    public BaseResponse<Boolean> isLogin() {
        return BaseResponse.success(StpUtil.isLogin());
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> logout() {
        StpUtil.logout();
        return BaseResponse.success(true);
    }

    @RequestMapping("test")
    public String test() {
        return "test";
    }

}
