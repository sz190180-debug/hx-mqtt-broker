package com.hx.mqtt.domain.req.warehouse;

import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓库查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WarehouseQueryReq extends BasePageReq {

    /**
     * 仓库名称
     */
    private String warehouseName;
}
