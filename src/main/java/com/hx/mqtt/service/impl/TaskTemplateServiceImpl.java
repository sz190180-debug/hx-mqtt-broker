package com.hx.mqtt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.TaskTemplate;
import com.hx.mqtt.mapper.TaskTemplateMapper;
import com.hx.mqtt.service.TaskTemplateService;
import org.springframework.stereotype.Service;

@Service
public class TaskTemplateServiceImpl extends ServiceImpl<TaskTemplateMapper, TaskTemplate> implements TaskTemplateService {
}
