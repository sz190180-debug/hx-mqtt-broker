package com.hx.mqtt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.Amr;
import com.hx.mqtt.mapper.AmrMapper;
import com.hx.mqtt.service.AmrService;
import org.springframework.stereotype.Service;

@Service
public class AmrServiceImpl extends ServiceImpl<AmrMapper, Amr> implements AmrService {
}