package com.hx.mqtt.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AmrTableDto {

    private Long amrId;

    private String alias;
}
