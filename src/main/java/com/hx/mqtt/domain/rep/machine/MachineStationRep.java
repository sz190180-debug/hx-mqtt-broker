package com.hx.mqtt.domain.rep.machine;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 机台响应
 */
@Data
public class MachineStationRep {

    /**
     * 机台ID
     */
    private Long stationId;

    /**
     * 机台名称
     */
    private String stationName;

    /**
     * 设备类型 1-机械手
     */
    private Integer deviceType;

    /**
     * 设备类型描述
     */
    private String deviceTypeDesc;

    /**
     * 所在地图ID
     */
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
    private Long taskChainTemplateId;

    /**
     * 通信首地址
     */
    private Long firstAddressId;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 关联的寄存器配置列表
     */
    private List<MachineStationRegisterRep> registers;
}
