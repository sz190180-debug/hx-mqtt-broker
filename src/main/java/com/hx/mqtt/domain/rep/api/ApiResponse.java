package com.hx.mqtt.domain.rep.api;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean state;
    private String errCode;
    private String errMsg;
    private T data;
}
