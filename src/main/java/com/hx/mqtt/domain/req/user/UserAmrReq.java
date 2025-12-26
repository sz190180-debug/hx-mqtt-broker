package com.hx.mqtt.domain.req.user;

import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserAmrReq extends BasePageReq {

    @NotNull(message = "用户id不能为空")
    private Long hxUserId;

}
