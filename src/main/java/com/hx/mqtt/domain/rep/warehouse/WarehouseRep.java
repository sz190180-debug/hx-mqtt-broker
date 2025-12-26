package com.hx.mqtt.domain.rep.warehouse;

import lombok.Data;

import java.util.List;

/**
 * 仓库响应
 */
@Data
public class WarehouseRep {

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 仓库描述
     */
    private String description;

    /**
     * 库位列列表
     */
    private List<WarehouseColumnRep> columns;
}
