package com.hx.mqtt.domain.req.rcs;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RcsAddReq {

    @NotNull(message = "rcs的ip地址不能为空")
    private String ip;

    @NotNull(message = "rcs的端口不能为空")
    private String port;

    @NotNull(message = "rcs的token不能为空")
    private String token;

    @NotNull(message = "rcs的名称不能为空")
    private String name;
}
