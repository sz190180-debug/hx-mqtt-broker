package com.hx.mqtt.domain.req.amr;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AmrMappingReq {

    @NotNull(message = "id不能为空")
    private Long id;
}
