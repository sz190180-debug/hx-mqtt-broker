package com.hx.mqtt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hx.mqtt.domain.entity.HxUserAmr;
import com.hx.mqtt.domain.rep.user.UserAmrRep;
import com.hx.mqtt.domain.req.user.UserAmrReq;

public interface HxUserAmrService extends IService<HxUserAmr> {
    IPage<UserAmrRep> selectAmr(UserAmrReq req);
}