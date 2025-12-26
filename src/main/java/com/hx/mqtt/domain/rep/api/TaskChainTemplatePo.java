package com.hx.mqtt.domain.rep.api;

import lombok.Data;

import java.util.Date;

/**
 * 任务链模板实体
 */
@Data
public class TaskChainTemplatePo {
    /**
     * 模板ID
     */
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 车ID
     */
    private Long amrId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建时间(时间戳)
     */
    private Date createTime;

    /**
     * 是否重复
     */
    private Boolean repeatFlag;
}