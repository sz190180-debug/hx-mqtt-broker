package com.hx.mqtt.domain.req.warehouse;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 添加仓库请求
 */
@Data
public class WarehouseAddReq {

    /**
     * 仓库名称
     */
    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;

    /**
     * 仓库描述
     */
    private String description;
}
