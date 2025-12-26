package com.hx.mqtt.domain.req.chain;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TaskChainUpdateReq {

    @NotNull(message = "id不能为空")
    private Long id;

    @NotNull(message = "别名不能为空")
    private String alias;
}
