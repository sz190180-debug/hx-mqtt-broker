package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("hx_user_task_chain_template")
public class HxUserTaskChainTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long hxUserId;
    private Long taskChainTemplateId;
    private String groupName;

    private Long lastTaskChainId;

    /**
     * 仓库ID，必填
     */
    private Long warehouseId;

    /**
     * 库位列ID，非必填
     */
    private Long columnId;

    /**
     * 排序字段，数值越小排序越靠前
     */
    private Integer sortOrder;
}