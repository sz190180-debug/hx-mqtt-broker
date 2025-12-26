package com.hx.mqtt.handler.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.domain.entity.HxUser;
import com.hx.mqtt.domain.req.api.BasePageMqttReq;
import com.hx.mqtt.domain.req.user.UserAmrReq;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.HxUserAmrService;
import com.hx.mqtt.service.HxUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAmrHandler implements MqttTopicHandler {

    private final HxUserService hxUserService;
    private final HxUserAmrService hxUserAmrService;

    @Override
    public TopicEnum getTopicEnum() {
        return TopicEnum.USER_AMR;
    }

    @Override
    public MqttResp<?> handle(MqttContext context, String payload) {
        BasePageMqttReq basePageMqttReq = BasePageMqttReq.fromJson(payload);
        String clientId = context.getClientId();
        LambdaQueryWrapper<HxUser> lq = Wrappers.lambdaQuery();
        lq.eq(HxUser::getClientId, clientId);
        HxUser hxUser = hxUserService.getOne(lq);
        if (hxUser == null) {
            throw new RuntimeException("未能找到用户");
        }
        UserAmrReq req = new UserAmrReq();
        req.setHxUserId(hxUser.getHxUserId());
        req.setPageNum(basePageMqttReq.getPageNum());
        req.setPageSize(basePageMqttReq.getPageSize());
        return MqttResp.success(context.getReqId(), hxUserAmrService.selectAmr(req));
    }
}
