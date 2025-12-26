package com.hx.mqtt.handler;

import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.TopicEnum;

public interface MqttTopicHandler {

    TopicEnum getTopicEnum();

    MqttResp<?> handle(MqttContext context, String payload);
}
