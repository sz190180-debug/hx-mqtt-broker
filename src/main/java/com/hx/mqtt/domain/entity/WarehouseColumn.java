package com.hx.mqtt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 库位列实体类
 */
@Data
@TableName("warehouse_column")
public class WarehouseColumn {

    /**
     * 库位列ID
     */
    @TableId(value = "column_id", type = IdType.AUTO)
    private Long columnId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 库位列名称
     */
    private String columnName;

    /**
     * 库位列排序
     */
    private Integer columnOrder;
}
