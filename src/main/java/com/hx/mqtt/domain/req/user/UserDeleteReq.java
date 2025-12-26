package com.hx.mqtt.domain.req.user;


import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserDeleteReq {

    @NotNull(message = "用户id不能为空")
    private String hxUserId;
}
