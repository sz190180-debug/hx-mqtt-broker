package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 任务链模板实体
 */
@Data
@TableName("task_chain_template")
public class TaskChainTemplate {
    /**
     * 模板ID
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 别名
     */
    private String alias;

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

    /**
     * 任务链类型
     * 入库属性（1），出库属性（2），普通（0）
     */
    private Integer chainType;
}