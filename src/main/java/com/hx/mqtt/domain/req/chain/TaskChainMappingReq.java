package com.hx.mqtt.domain.req.chain;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TaskChainMappingReq {

    @NotNull(message = "id不能为空")
    private Long id;
}
