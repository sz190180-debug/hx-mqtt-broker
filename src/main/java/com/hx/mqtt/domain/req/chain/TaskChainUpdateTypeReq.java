package com.hx.mqtt.domain.req.chain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TaskChainUpdateTypeReq {

    @NotEmpty(message = "id不能为空")
    private List<Long> ids;

    @NotNull(message = "任务链类型不能为空")
    private Integer chainType;
}
