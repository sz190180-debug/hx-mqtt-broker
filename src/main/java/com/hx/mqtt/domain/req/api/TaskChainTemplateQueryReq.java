package com.hx.mqtt.domain.req.api;

import lombok.Data;

/**
 * 任务模板查询参数实体类
 */
@Data
public class TaskChainTemplateQueryReq {
    /**
     * 分组ID，指定分组下的任务链模板
     */
    private Long groupId;

    /**
     * 任务模板名称（模糊查询）
     */
    private String name;

    /**
     * 当前页码，默认1
     */
    private Integer currentPage = 1;

    /**
     * 每页行数，最大默认10000条
     */
    private Integer pageSize = 10000;
}