package com.hx.mqtt.domain.rep.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下拉选项响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectOption {

    /**
     * 选项值
     */
    private Object value;

    /**
     * 选项标签
     */
    private String label;

    /**
     * 是否禁用
     */
    private Boolean disabled;

    public SelectOption(Object value, String label) {
        this.value = value;
        this.label = label;
        this.disabled = false;
    }
}
