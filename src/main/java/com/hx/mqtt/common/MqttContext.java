package com.hx.mqtt.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MqttContext {
    private String clientId;
    private Long reqId;
    private Boolean sendRep;
}
