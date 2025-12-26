package com.hx.mqtt.domain.req.amr;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AmrTableUpdateReq {

    @NotNull(message = "车辆id不能为空")
    private Long amrId;

    private String alias;
}
