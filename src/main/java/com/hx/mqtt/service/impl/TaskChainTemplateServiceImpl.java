package com.hx.mqtt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.TaskChainTemplate;
import com.hx.mqtt.mapper.TaskChainTemplateMapper;
import com.hx.mqtt.service.TaskChainTemplateService;
import org.springframework.stereotype.Service;

@Service
public class TaskChainTemplateServiceImpl extends ServiceImpl<TaskChainTemplateMapper, TaskChainTemplate> implements TaskChainTemplateService {
}
