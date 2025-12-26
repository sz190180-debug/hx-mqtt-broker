package com.hx.mqtt.domain.req.user;


import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserAddReq {

    @NotNull(message = "clientId不能为空")
    private String clientId;

    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    private String username;

    private String password;

    private String remark;
}
