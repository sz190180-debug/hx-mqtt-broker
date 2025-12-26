package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 机台管理实体类
 */
@Data
@TableName("machine_station")
public class MachineStation {

    /**
     * 机台ID
     */
    @TableId(value = "station_id", type = IdType.AUTO)
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
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;
}
