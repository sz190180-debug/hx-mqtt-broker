package com.hx.mqtt.service;

import com.hx.mqtt.common.enums.TopicEnum;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;

public interface MessageDispatchService {
    void handleMessage(Message<?> message);

    void broadcastMessage(@NotNull TopicEnum topicEnum, Object payload, Object... path);

    void sendClientMessage(@NotNull TopicEnum topicEnum, @NotNull String clientId, Object payload);
}