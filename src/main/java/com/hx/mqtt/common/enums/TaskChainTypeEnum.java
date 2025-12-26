package com.hx.mqtt.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 任务链类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum TaskChainTypeEnum {

    /**
     * 普通任务链
     */
    NORMAL(0, "普通"),

    /**
     * 入库任务链
     */
    INBOUND(1, "入库"),

    /**
     * 出库任务链
     */
    OUTBOUND(2, "出库");

    private final Integer code;
    private final String description;

    /**
     * 根据代码获取对应的枚举值
     *
     * @param code 代码
     * @return 对应的枚举值，如果没有匹配的则返回null
     */
    public static TaskChainTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskChainTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否需要仓库信息
     * 入库和出库任务链需要仓库信息，普通任务链不需要
     *
     * @return true-需要仓库信息，false-不需要仓库信息
     */
    public boolean requiresWarehouse() {
        return this == INBOUND || this == OUTBOUND;
    }
}
