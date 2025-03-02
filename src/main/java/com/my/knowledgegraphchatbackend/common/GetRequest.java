package com.my.knowledgegraphchatbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 根据userId查询请求
 *

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