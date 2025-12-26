package com.hx.mqtt.domain.req.user;


import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserEditReq {

    @NotNull(message = "用户id不能为空")
    private Long hxUserId;

    @NotNull(message = "clientId不能为空")
    private String clientId;

    private Integer userType;

    private String username;

    private String password;

    private String remark;
}
