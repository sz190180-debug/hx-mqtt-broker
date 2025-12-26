package com.hx.mqtt.domain.req.warehouse;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新库位列点位关联请求
 */
@Data
public class WarehouseColumnVertexesUpdateReq {

    /**
     * 关联ID
     */
    @NotNull(message = "关联ID不能为空")
    private Long positionId;

    /**
     * 库位列ID
     */
    @NotNull(message = "库位列ID不能为空")
    private Long columnId;

    /**
     * 地图点位ID
     */
    @NotNull(message = "地图点位ID不能为空")
    private Long hxMapVertexesId;

    /**
     * 点位排序
     */
    @NotNull(message = "点位排序不能为空")
    private Integer positionOrder;

    /**
     * 状态：1-可用，2-占用，3-禁用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
