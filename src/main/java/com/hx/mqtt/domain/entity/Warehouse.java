package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 仓库实体类
 */
@Data
@TableName("warehouse")
public class Warehouse {

    /**
     * 仓库ID
     */
    @TableId(value = "warehouse_id", type = IdType.AUTO)
    private Long warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 仓库描述
     */
    private String description;
}
