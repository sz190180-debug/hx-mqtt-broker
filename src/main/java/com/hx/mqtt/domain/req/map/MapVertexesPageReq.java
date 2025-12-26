package com.hx.mqtt.domain.req.map;

import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class MapVertexesPageReq extends BasePageReq {

    @NotNull(message = "地图id不能为空")
    private Long mapId;

    private String code;
}
