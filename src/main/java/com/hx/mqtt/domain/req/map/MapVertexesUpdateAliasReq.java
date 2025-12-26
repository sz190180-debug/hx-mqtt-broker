package com.hx.mqtt.domain.req.map;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MapVertexesUpdateAliasReq {

    @NotNull(message = "点位id不能为空")
    private Long id;

    private String codeAlias;
}
