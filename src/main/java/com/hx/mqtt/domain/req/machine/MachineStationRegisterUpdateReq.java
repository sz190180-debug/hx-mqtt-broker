package com.hx.mqtt.domain.req.machine;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新机台寄存器配置请求
 */
@Data
public class MachineStationRegisterUpdateReq {

    /**
     * 寄存器ID
     */
    @NotNull(message = "寄存器ID不能为空")
    private Long registerId;

    /**
     * 机台ID
     */
    @NotNull(message = "机台ID不能为空")
    private Long stationId;

    /**
     * 寄存器类型：CONTROL, REQUEST_ENTER, ALLOW_ENTER, AGV_EXIT
     */
    @NotNull(message = "寄存器类型不能为空")
    private Integer registerType;

    /**
     * 寄存器地址
     */
    @NotNull(message = "寄存器地址不能为空")
    private Integer registerAddress;

    /**
     * 协议类型：MODBUS_TCP
     */
    @NotBlank(message = "协议类型不能为空")
    private String protocolType;

    /**
     * 寄存器描述
     */
    private String description;

    /**
     * 状态：1-启用，0-禁用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 点位ID（可选）
     */
    private Long vertexId;

    /**
     * 点位编码（可选）
     */
    private String vertexCode;

    /**
     * 控制类型（仅控制寄存器使用）：0-无动作 1-右叫车 2-右叫料 3-左叫车 4-左叫料
     */
    private Integer controlType;

    /**
     * 控制值
     */
    private Integer controlValue;

    /**
     * 叫料点位ID（可选）
     */
    private Long callVertexId;

    /**
     * 叫料点位编码（可选）
     */
    private String callVertexCode;
}
