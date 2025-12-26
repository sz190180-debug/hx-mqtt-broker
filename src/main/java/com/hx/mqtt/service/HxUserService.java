package com.hx.mqtt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hx.mqtt.domain.entity.HxUser;

public interface HxUserService extends IService<HxUser> {

    HxUser getByClientId(String clientId);
}
