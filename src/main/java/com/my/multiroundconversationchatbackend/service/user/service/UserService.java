package com.my.multiroundconversationchatbackend.service.user.service;

/**
 * @author lihaixu
 * @date 2025年03月15日 20:33
 */

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.my.multiroundconversationchatbackend.common.BaseResponse;
import com.my.multiroundconversationchatbackend.common.ErrorCode;
import com.my.multiroundconversationchatbackend.common.user.request.UserModifyRequest;

import com.my.multiroundconversationchatbackend.enums.user.UserStateEnum;
import com.my.multiroundconversationchatbackend.exception.BusinessException;
import com.my.multiroundconversationchatbackend.mapper.UserMapper;
import com.my.multiroundconversationchatbackend.model.entity.user.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 用户服务
 *
 * @author hollis
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User>{

    private static final String DEFAULT_NICK_NAME_PREFIX = "藏家_";

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public BaseResponse<Boolean> register(String telephone) {
        String defaultNickName;
        String randomString;
        do {
            randomString = RandomUtil.randomString(6).toUpperCase();
            //前缀 + 6位随机数 + 手机号后四位
            defaultNickName = DEFAULT_NICK_NAME_PREFIX + randomString + telephone.substring(7, 11);
        } while (nickNameExist(defaultNickName));

        User user = register(telephone, defaultNickName, telephone);
        Assert.notNull(user, () -> new BusinessException(ErrorCode.SYSTEM_ERROR));

        //加入流水
        //todo
        return BaseResponse.success(true);
    }


    /**
     * 注册
     *
     * @param telephone
     * @param nickName
     * @param password
     * @return
     */
    private User register(String telephone, String nickName, String password) {
        if (userMapper.findByTelephone(telephone) != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_TELEPHONE_NUMBER);
        }

        User user = new User();
        user.register(telephone, nickName, password);
        return save(user) ? user : null;
    }

    private User registerAdmin(String telephone, String nickName, String password) {
        if (userMapper.findByTelephone(telephone) != null) {
            throw new BusinessException(ErrorCode.DUPLICATE_TELEPHONE_NUMBER);
        }

        User user = new User();
        user.registerAdmin(telephone, nickName, password);
        return save(user) ? user : null;
    }


    /**
     * 冻结
     *
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> freeze(Long userId) {
        User user = userMapper.findById(userId);
        Assert.notNull(user, () -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        Assert.isTrue(user.getState() == UserStateEnum.ACTIVE, () -> new BusinessException(ErrorCode.USER_STATUS_IS_NOT_ACTIVE));

        //第一次删除缓存
        //todo

        if (user.getState() == UserStateEnum.FROZEN) {
            return BaseResponse.success(true);
        }
        user.setState(UserStateEnum.FROZEN);
        boolean updateResult = updateById(user);
        Assert.isTrue(updateResult, () -> new BusinessException(ErrorCode.SYSTEM_ERROR));
        //加入流水
        //todo
        return BaseResponse.success(true);
    }

    /**
     * 解冻
     *
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> unfreeze(Long userId) {
        User user = userMapper.findById(userId);
        Assert.notNull(user, () -> new BusinessException(ErrorCode.USER_NOT_EXIST));

        //第一次删除缓存
        //todo

        if (user.getState() == UserStateEnum.ACTIVE) {
            return BaseResponse.success(true);
        }
        user.setState(UserStateEnum.ACTIVE);
        //更新数据库
        boolean updateResult = updateById(user);
        Assert.isTrue(updateResult, () -> new BusinessException(ErrorCode.SYSTEM_ERROR));
        //加入流水
        //todo

        //第二次删除缓存
        //todo
        return BaseResponse.success(true);
    }


    /**
     * 通过手机号和密码查询用户信息
     *
     * @param telephone
     * @param password
     * @return
     */
    public User findByTelephoneAndPass(String telephone, String password) {
        return userMapper.findByTelephoneAndPass(telephone, DigestUtil.md5Hex(password));
    }

    /**
     * 通过手机号查询用户信息
     *
     * @param telephone
     * @return
     */
    public User findByTelephone(String telephone) {
        return userMapper.findByTelephone(telephone);
    }

    /**
     * 通过用户ID查询用户信息
     *
     * @param userId
     * @return
     */
    public User findById(Long userId) {
        return userMapper.findById(userId);
    }

    /**
     * 更新用户信息
     *
     * @param userModifyRequest
     * @return
     */
    @Transactional
    public BaseResponse<Boolean> modify(UserModifyRequest userModifyRequest) {
        User user = userMapper.findById(userModifyRequest.getUserId());
        Assert.notNull(user, () -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        Assert.isTrue(user.canModifyInfo(), () -> new BusinessException(ErrorCode.USER_STATUS_CANT_OPERATE));

        if (StringUtils.isNotBlank(userModifyRequest.getNickName()) && nickNameExist(userModifyRequest.getNickName())) {
            throw new BusinessException(ErrorCode.NICK_NAME_EXIST);
        }
        BeanUtils.copyProperties(userModifyRequest, user);

        if (StringUtils.isNotBlank(userModifyRequest.getPassword())) {
            user.setPasswordHash(DigestUtil.md5Hex(userModifyRequest.getPassword()));
        }
        if (updateById(user)) {
            //加入流水
           //todo
            return BaseResponse.success(true);
        }
        return BaseResponse.error(500,"修改失败");
    }

    public boolean nickNameExist(String nickName) {
        return userMapper.findByNickname(nickName) != null;
    }

}
