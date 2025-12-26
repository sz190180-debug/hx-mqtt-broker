package com.hx.mqtt.domain.req.machine;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新机台请求
 */
@Data
public class MachineStationUpdateReq {

    /**
     * 机台ID
     */
    @NotNull(message = "机台ID不能为空")
    private Long stationId;

    /**
     * 机台名称
     */
    @NotBlank(message = "机台名称不能为空")
    private String stationName;

    /**
     * 设备类型 1-机械手
     */
    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    /**
     * 所在地图ID
     */
    @NotNull(message = "地图ID不能为空")
    private Long mapId;

    /**
     * 所在区域ID
     */
    private Long areaId;

    /**
     * 机台描述
     */
    private String description;

    /**
     * 绑定的入库任务链模板ID
     */
    @NotNull(message = "任务链模板ID不能为空")
    private Long taskChainTemplateId;

    /**
     * 通信首地址
     */
    private Long firstAddressId;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
}
