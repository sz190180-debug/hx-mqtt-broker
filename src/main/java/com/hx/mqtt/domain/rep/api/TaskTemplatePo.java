package com.hx.mqtt.domain.rep.api;

import lombok.Data;

/**
 * 子任务模板实体
 */
@Data
public class TaskTemplatePo {
    /**
     * 子任务ID
     */
    private Long id;

    /**
     * 任务链模板ID
     */
    private Long taskChainTemplateId;

    /**
     * 目标点编号
     */
    private String endPointCode;

    /**
     * 地图ID
     */
    private Long mapId;

    /**
     * 上下料属性
     */
    private Integer loading;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 序号
     */
    private Double sequence;

    /**
     * 对接方向
     */
    private Integer dockingDirection;

    /**
     * 对接X距离
     */
    private Double dockingX;

    /**
     * 对接Y距离
     */
    private Double dockingY;

    /**
     * 扩展参数(JSON格式)
     */
    private String extend;
}