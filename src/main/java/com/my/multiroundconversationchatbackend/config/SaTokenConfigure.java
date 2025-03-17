//package com.my.multiroundconversationchatbackend.config;
//
///**
// * @author lihaixu
// * @date 2025年03月15日 22:21
// */
//
//import cn.dev33.satoken.exception.NotLoginException;
//import cn.dev33.satoken.reactor.filter.SaReactorFilter;
//import cn.dev33.satoken.router.SaRouter;
//import cn.dev33.satoken.stp.StpUtil;
//import cn.dev33.satoken.util.SaResult;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * sa-token的全局配置
// *
// * @author Hollis
// */
//@Configuration
//@Slf4j
//public class SaTokenConfigure {
//
//    @Bean
//    public SaReactorFilter getSaReactorFilter() {
//        return new SaReactorFilter()
//                // 拦截地址
//                .addInclude("/**")
//                // 开放地址
//                .addExclude("/favicon.ico")
//                // 鉴权方法：每次访问进入
//                .setAuth(obj -> {
//                    // 登录校验 -- 拦截所有路由，并排除/auth/login 用于开放登录
//                    SaRouter.match("/**").notMatch("/auth/**").check(r -> StpUtil.checkLogin());
//                })
//                // 异常处理方法：每次setAuth函数出现异常时进入
//                .setError(this::getSaResult);
//    }
//
//    private SaResult getSaResult(Throwable throwable) {
//        if (throwable instanceof NotLoginException) {
//            log.error("请先登录");
//            return SaResult.error("请先登录");
//        }
//        return SaResult.error(throwable.getMessage());
//    }
//}
