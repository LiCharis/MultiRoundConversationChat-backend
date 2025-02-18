package com.my.knowledgegraphchatbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 根据userId查询请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class GetRequest implements Serializable {


    /**
     * Id
     */
    private String id;

    /**
     * userId
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}