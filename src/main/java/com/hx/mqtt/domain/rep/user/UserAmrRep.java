package com.hx.mqtt.domain.rep.user;

import lombok.Data;

@Data
public class UserAmrRep {

    private Long id;

    private Long hxUserId;

    /**
     * 车辆id
     */
    private Long amrId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 别名
     */
    private String alias;

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
}
