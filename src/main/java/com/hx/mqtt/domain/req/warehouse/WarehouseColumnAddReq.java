package com.hx.mqtt.domain.req.warehouse;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 添加库位列请求
 */
@Data
public class WarehouseColumnAddReq {

    /**
     * 仓库ID
     */
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /**
     * 库位列名称
     */
    @NotBlank(message = "库位列名称不能为空")
    private String columnName;

    /**
     * 库位列排序
     */
    @NotNull(message = "库位列排序不能为空")
    private Integer columnOrder;
}
