package com.hx.mqtt.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 任务链状态枚举
 */
@Getter
@RequiredArgsConstructor
public enum TaskChainStatusEnum {

    /**
     * 未执行
     */
    NOT_EXECUTED(0, "未执行"),

    /**
     * 正在执行
     */
    EXECUTING(1, "正在执行"),

    /**
     * 已完成
     */
    COMPLETED(2, "已完成"),

    /**
     * 取消
     */
    CANCELLED(3, "取消"),

    /**
     * 异常
     */
    EXCEPTION(4, "异常"),

    /**
     * 跳过
     */
    SKIPPED(5, "跳过"),

    /**
     * 暂停
     */
    PAUSED(6, "暂停");

    private final Integer code;
    private final String description;

    /**
     * 根据状态码获取对应的枚举值
     *
     * @param code 状态码
     * @return 对应的枚举值，如果没有匹配的则返回null
     */
    public static TaskChainStatusEnum getByCode(int code) {
        for (TaskChainStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}