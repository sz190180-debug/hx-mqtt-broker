package com.hx.mqtt.domain.req.amr;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class AmrRegisterConfigUpdateReq {
    @NotNull(message = "ID不能为空")
    private Long id;

    private Long amrId;
    private String ip;
    private Integer port;
    private Integer readAddress;
    private Integer writeAddress;
}