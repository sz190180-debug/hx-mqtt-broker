package com.hx.mqtt.domain.dto;

import lombok.Data;

@Data
public class TaskTemplateBroadcastDto {

    /**
     * 任务链类型0/1/2
     */
    private Integer chainType;

    private Long amrId;

    private Long taskChainId;
}
