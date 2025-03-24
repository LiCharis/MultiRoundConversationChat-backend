package com.my.multiroundconversationchatbackend.common;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 删除请求
 *

 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    @NotNull(message = "删除id为null")
    private String id;

    private static final long serialVersionUID = 1L;
}