package com.hx.mqtt.domain.rep.api;

import lombok.Data;

import java.util.List;

/**
 * 分页查询结果
 */
@Data
public class TaskChainPageRep {
    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据列表
     */
    private List<TaskChainTemplateWrapper> list;
}