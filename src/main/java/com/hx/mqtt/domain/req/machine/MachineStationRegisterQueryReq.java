package com.hx.mqtt.domain.req.machine;

import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 机台寄存器配置查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MachineStationRegisterQueryReq extends BasePageReq {

    /**
     * 机台ID
     */
    private Long stationId;

    /**
     * 寄存器类型：CONTROL, REQUEST_ENTER, ALLOW_ENTER, AGV_EXIT
     */
    private Integer registerType;

    /**
     * 协议类型：MODBUS_TCP
     */
    private String protocolType;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 点位编码
     */
    private String vertexCode;
}
