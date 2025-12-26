package com.hx.mqtt.domain.rep.mqtt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MsgRep {

    /**
     * 消息
     */
    private String msg;
}
