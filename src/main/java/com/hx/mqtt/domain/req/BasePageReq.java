package com.hx.mqtt.domain.req;

import lombok.Data;

@Data
public class BasePageReq {
    private Long pageNum = 1L;

    private Long pageSize = 100L;
}
