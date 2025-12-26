package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 库位列点位关联实体类
 */
@Data
@TableName("warehouse_column_vertexes")
public class WarehouseColumnVertexes {

    /**
     * 关联ID
     */
    @TableId(value = "position_id", type = IdType.AUTO)
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
}
