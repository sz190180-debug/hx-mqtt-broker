package com.hx.mqtt.domain.req.map;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MapDeleteReq {

    @NotNull(message = "地图id不能为空")
    private Long mapId;
}
