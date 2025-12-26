package com.hx.mqtt.domain.rep.machine;

import lombok.Data;

import java.util.Date;

/**
 * 机台寄存器配置响应
 */
@Data
public class MachineStationRegisterRep {

    /**
     * 寄存器ID
     */
    private Long registerId;

    /**
     * 机台ID
     */
    private Long stationId;

    /**
     * 寄存器类型：CONTROL, REQUEST_ENTER, ALLOW_ENTER, AGV_EXIT
     */
    private Integer registerType;

    /**
     * 寄存器类型描述
     */
    private String registerTypeDesc;

    /**
     * 寄存器地址
     */
    private Integer registerAddress;

    /**
     * 控制类型（仅控制寄存器使用）：0-无动作 1-右叫车 2-右叫料 3-左叫车 4-左叫料
     */
    private Integer controlType;

    /**
     * 控制值
     */
    private Integer controlValue;

    /**
     * 协议类型：MODBUS_TCP
     */
    private String protocolType;

    /**
     * 寄存器描述
     */
    private String description;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 点位ID
     */
    private Long vertexId;

    /**
     * 点位编码
     */
    private String vertexCode;

    /**
     * 叫料点位ID
     */
    private Long callVertexId;

    /**
     * 叫料点位编码
     */
    private String callVertexCode;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;
}
