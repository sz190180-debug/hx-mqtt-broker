package com.hx.mqtt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.HxUser;
import com.hx.mqtt.mapper.HxUserMapper;
import com.hx.mqtt.service.HxUserService;
import org.springframework.stereotype.Service;

@Service
public class HxUserServiceImpl extends ServiceImpl<HxUserMapper, HxUser> implements HxUserService {

    @Override
    public HxUser getByClientId(String clientId) {
        LambdaQueryWrapper<HxUser> lq = Wrappers.lambdaQuery();
        lq.eq(HxUser::getClientId, clientId);
        return getOne(lq);
    }
}
