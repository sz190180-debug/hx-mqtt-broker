package com.hx.mqtt.domain.req.chain;

import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskChainListReq extends BasePageReq {

    private String name;

    private String alias;
}
