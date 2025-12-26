package com.hx.mqtt.domain.dto;

import lombok.Data;

@Data
public class GetTaskChainsDto {

    private Integer status;

    private Long taskChainId;
}
