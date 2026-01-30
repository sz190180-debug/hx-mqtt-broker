package com.hx.mqtt.domain.req.amr;

import com.hx.mqtt.domain.req.BasePageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AmrRegisterConfigPageReq extends BasePageReq {
    private Long amrId; // 可选：按车辆ID筛选
}