package com.hx.mqtt.domain.dto;

/**
 * 机台控制状态枚举
 */
public enum MachineControlStep {
    IDLE,                   // 空闲状态
    MOVING_TO_REQUEST,      // 移动到请求点
    REQUESTING_ENTER,       // 请求进入
    LOADING_AND_MOVING,     // 上/下料并移动
    SENDING_EXIT_SIGNAL,    // 发送退出信号
    TRIGGERING_INBOUND      // 触发入库任务链
}