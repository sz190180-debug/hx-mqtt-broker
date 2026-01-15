package com.hx.mqtt.domain.dto;

import lombok.Data;

@Data
public class AmrDataDto {
    /**
     * 车辆id
     */
    private Long id;
    /**
     * x坐标
     */
    private Double x;
    /**
     * y坐标
     */
    private Double y;
    /**
     * 角度
     */
    private Double theta;
    /**
     * 车辆状态
     * IDLE(1, "空闲"),
     * WORKING(2, "工作中"),
     * CHARGING(3, "充电中"),
     * TRAFFIC_WAITING(4, "交管等待"),
     * ESCROW(5, "托管中"),
     * EXTERNAL_INTERRUPTION(6, "外部中断"),
     * UNKNOWN(7, "未知"),
     * ABNORMAL_PAUSE(8, "异常暂停"),
     * WAITING_SCHEDULE_CONFIRMATION(1000, "等待调度确认")
     */
    private Integer state;
    /**
     * 电池电量
     */
    private Double batteryPercentile;
    /**
     * 是否载物
     */
    private Boolean materials;
    /**
     * 任务链id
     */
    private Long taskChainId;

    private String name;

    private int present;
}
