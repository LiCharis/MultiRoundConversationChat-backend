package com.my.multiroundconversationchatbackend.common.user.validator;

/**
 * @author lihaixu
 * @date 2025年03月15日 20:01
 */

import cn.hutool.core.lang.Validator;
import com.my.multiroundconversationchatbackend.annotation.IsMobile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 手机号校验器
 * @author lihaixu
 */
public class MobileValidator implements ConstraintValidator<IsMobile, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return Validator.isMobile(value);
    }
}
