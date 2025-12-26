package com.hx.mqtt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.HxMap;
import com.hx.mqtt.mapper.HxMapMapper;
import com.hx.mqtt.service.HxMapService;
import org.springframework.stereotype.Service;

@Service
public class HxMapServiceImpl extends ServiceImpl<HxMapMapper, HxMap> implements HxMapService {
}
