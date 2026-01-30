package com.hx.mqtt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.AmrRegisterConfig;
import com.hx.mqtt.mapper.AmrRegisterConfigMapper;
import com.hx.mqtt.service.AmrRegisterConfigService;
import org.springframework.stereotype.Service;

@Service
public class AmrRegisterConfigServiceImpl extends ServiceImpl<AmrRegisterConfigMapper, AmrRegisterConfig> implements AmrRegisterConfigService {
}