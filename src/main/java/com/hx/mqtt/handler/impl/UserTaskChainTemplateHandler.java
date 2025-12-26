package com.hx.mqtt.handler.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.entity.HxUser;
import com.hx.mqtt.domain.req.api.UserTaskChainTemplateMqttReq;
import com.hx.mqtt.domain.req.user.UserTaskChainTemplateReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.HxUserService;
import com.hx.mqtt.service.HxUserTaskChainTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTaskChainTemplateHandler implements MqttTopicHandler {

    private final HxUserService hxUserService;
    private final HxUserTaskChainTemplateService hxUserTaskChainTemplateService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.USER_TASK_CHAIN_TEMPLATE;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        UserTaskChainTemplateMqttReq req = UserTaskChainTemplateMqttReq.fromJson(payload);
        String clientId = context.getClientId();
        LambdaQueryWrapper<HxUser> lq = Wrappers.lambdaQuery();
        lq.eq(HxUser::getClientId, clientId);
        HxUser hxUser = hxUserService.getOne(lq);
        if (hxUser == null) {
            throw new RuntimeException("未能找到用户");
        }
        UserTaskChainTemplateReq templateReq = new UserTaskChainTemplateReq();
        templateReq.setHxUserId(hxUser.getHxUserId());
        templateReq.setGroupName(req.getGroupName());
        templateReq.setPageNum(req.getPageNum());
        templateReq.setPageSize(req.getPageSize());
        templateReq.setSortOrder(req.getSortOrder());
        return MqttResp.success(context.getReqId(), hxUserTaskChainTemplateService.selectTaskChain(templateReq));
    }
}
