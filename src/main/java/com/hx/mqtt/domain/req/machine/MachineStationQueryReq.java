package com.hx.mqtt.domain.req.machine;

import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 机台查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MachineStationQueryReq extends BasePageReq {

    /**
     * 机台名称
     */
    private String stationName;

    /**
     * 设备类型 1-机械手
     */
    private Integer deviceType;

    /**
     * 所在地图ID
     */
    private Long mapId;

    /**
     * 所在区域ID
     */
    private Long areaId;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
}
