package com.hx.mqtt.domain.req.amr;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class AmrRegisterConfigAddReq {
    @NotNull(message = "车辆ID不能为空")
    private Long amrId;

    @NotNull(message = "IP不能为空")
    private String ip;

    @NotNull(message = "端口不能为空")
    private Integer port;

    @NotNull(message = "读寄存器地址不能为空")
    private Integer readAddress;

    @NotNull(message = "写寄存器地址不能为空")
    private Integer writeAddress;
}