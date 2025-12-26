package com.hx.mqtt.handler.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.entity.HxUser;
import com.hx.mqtt.domain.entity.HxUserTaskChainTemplate;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.HxUserService;
import com.hx.mqtt.service.HxUserTaskChainTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserTaskChainGroupHandler implements MqttTopicHandler {

    private final HxUserService hxUserService;
    private final HxUserTaskChainTemplateService hxUserTaskChainTemplateService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.USER_TASK_CHAIN_GROUP;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        HxUser hxUser = hxUserService.getByClientId(context.getClientId());
        if (hxUser == null) {
            throw new RuntimeException("未能找到用户");
        }

        LambdaQueryWrapper<HxUserTaskChainTemplate> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(HxUserTaskChainTemplate::getGroupName)
                .eq(HxUserTaskChainTemplate::getHxUserId, hxUser.getHxUserId())
                .isNotNull(HxUserTaskChainTemplate::getGroupName)
                .groupBy(HxUserTaskChainTemplate::getGroupName);

        return MqttResp.success(context.getReqId(),
                hxUserTaskChainTemplateService.list(queryWrapper));
    }
}
