package com.hx.mqtt.domain.rep.warehouse;

import com.hx.mqtt.domain.entity.HxMapVertexes;
import lombok.Data;

/**
 * 库位列点位关联响应
 */
@Data
public class WarehouseColumnVertexesRep {

    /**
     * 关联ID
     */
    private Long positionId;

    /**
     * 库位列ID
     */
    private Long columnId;

    /**
     * 地图点位ID
     */
    private Long hxMapVertexesId;

    /**
     * 点位排序
     */
    private Integer positionOrder;

    /**
     * 状态：1-可用，2-占用，3-禁用
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 关联的地图点位信息
     */
    private HxMapVertexes mapVertex;
}
