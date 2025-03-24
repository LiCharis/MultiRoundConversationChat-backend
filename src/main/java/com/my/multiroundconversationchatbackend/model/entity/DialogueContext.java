package com.my.multiroundconversationchatbackend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lihaixu
 * @date 2025年03月22日 21:05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueContext {
    private String query;
    private String response;
    private List<DialogueRecord> history;
    private HttpSession session;
    private Map<String, Object> attributes;
    private String modelName;

    // 添加属性
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }

    // 获取属性
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        if (attributes == null) {
            return null;
        }
        return (T) attributes.get(key);
    }
}
