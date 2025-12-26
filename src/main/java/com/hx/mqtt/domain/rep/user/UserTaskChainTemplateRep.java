package com.hx.mqtt.domain.rep.user;

import lombok.Data;

@Data
public class UserTaskChainTemplateRep {

    private Long id;

    private Long hxUserId;

    /**
     * 任务链模板ID
     */
    private Long taskChainTemplateId;

    private Long lastTaskChainId;
    /**
     * 模板名称
     */
    private String name;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 别名
     */
    private String alias;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 库位列ID
     */
    private Long columnId;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 库位列名称
     */
    private String columnName;

    /**
     * 排序字段，数值越小排序越靠前
     */
    private Integer sortOrder;
}
