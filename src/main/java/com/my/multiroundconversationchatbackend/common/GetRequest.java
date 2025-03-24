package com.my.multiroundconversationchatbackend.common;

import lombok.Data;

import javax.validation.constraints.NotNull;
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
    @NotNull(message = "查询userId为null")
    private Long userId;

    private static final long serialVersionUID = 1L;
}