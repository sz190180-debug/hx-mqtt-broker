package com.hx.mqtt.domain.rep.warehouse;

import lombok.Data;

import java.util.List;

/**
 * 库位列响应
 */
@Data
public class WarehouseColumnRep {

    /**
     * 库位列ID
     */
    private Long columnId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 库位列名称
     */
    private String columnName;

    /**
     * 库位列排序
     */
    private Integer columnOrder;

    /**
     * 关联的点位列表
     */
    private List<WarehouseColumnVertexesRep> vertexes;
}
