package com.hx.mqtt.domain.req.api;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务链信息类
 */
@Data
public class TaskChainInfoReq implements Serializable {
    /**
     * 任务链 ID（由上层系统提供）
     */
    private Long taskChainId;

    /**
     * 区域 ID（由上层系统提供）
     */
    private Long areaId;

    /**
     * 任务创建时间（由上层系统提供）
     */
    private Long createTime;

    /**
     * 任务链开始时间
     */
    private Long chainStartTime;

    /**
     * 任务链结束时间
     */
    private Long chainFinishTime;

    /**
     * 任务来源
     */
    private String formDetail;

    /**
     * 车辆 ID
     */
    private Long amrId;

    /**
     * 任务状态:
     * 0-未执行;
     * 1-子任务正在执行;
     * 2-子任务已完成;
     * 3-任务链取消;
     * 4-子任务异常；
     * 5-任务链跳过；
     * 6-任务链异常；
     * 7-任务链完成
     */
    private Integer status;

    /**
     * 子任务 ID
     */
    private Long taskId;

    /**
     * 小车实际动作类型
     */
    private Integer action;

    /**
     * 子任务类型
     */
    private String taskType;

    /**
     * 目标点编号
     */
    private String endPointCode;

    /**
     * 地图 id
     */
    private Long mapId;

    /**
     * 子任务开始时间
     */
    private Long startTime;

    /**
     * 子任务结束时间
     */
    private Long finishTime;

    /**
     * 上下料属性 0上料 1下料
     */
    private Integer loading;

    /**
     * 任务拓展字段
     */
    private String extend;
}