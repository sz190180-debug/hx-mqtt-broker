package com.hx.mqtt.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.common.MqttClientManager;
import com.hx.mqtt.common.MqttContext;
import com.hx.mqtt.common.MqttResp;
import com.hx.mqtt.common.enums.MqttRespEnum;
import com.hx.mqtt.common.enums.TopicEnum;
import com.hx.mqtt.handler.MqttTopicHandler;
import com.hx.mqtt.service.MessageDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hx.mqtt.common.GlobalConstant.GLOBAL_PATH;
import static com.hx.mqtt.common.enums.MqttRespEnum.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageDispatchServiceImpl implements MessageDispatchService {

    private static final Pattern TOPIC_PATTERN = Pattern.compile("/iot/(?<clientId>[^/]+)/req/.*");
    private final MessageChannel mqttOutboundChannel;
    private final Map<TopicEnum, MqttTopicHandler> topicHandlers = new ConcurrentHashMap<>();
    private final ApplicationContext context;
    private final AtomicLong brokerReqId = new AtomicLong(0);

    @PostConstruct
    public void init() {
        Map<String, MqttTopicHandler> beansOfType = context.getBeansOfType(MqttTopicHandler.class);
        context.getBeansOfType(MqttTopicHandler.class);
        beansOfType.values().forEach(v -> topicHandlers.put(v.getTopicEnum(), v));
    }

    /**
     * 消息接收处理（支持动态路由）
     */
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    @Override
    public void handleMessage(Message<?> message) {
        log.info("Received message: Topic={}, Payload={}",
                message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC),
                message.getPayload());

        String payload = message.getPayload().toString();
        JSONObject payloadObject = JSONObject.parseObject(payload).getJSONObject("d");
        Long reqId = payloadObject.getLong("reqId");
        if (reqId == null) {
            sendError(null, NONE_EXIST_REQ_ID);
            return;
        }

        // 1. 解析基础信息
        String requestTopic = getHeader(message);
        if (StrUtil.isBlank(requestTopic)) {
            sendError(reqId, NONE_EXIST_TOPIC);
            return;
        }

        // 2. 提取客户端ID
        String clientId = parseClientId(requestTopic);
        if (StrUtil.isBlank(clientId)) {
            sendError(reqId, NONE_EXIST_CLIENT);
            return;
        } else if (!MqttClientManager.addReqId(clientId, reqId)) {
            log.warn("filter client {} req {}", clientId, payload);
            return;
        }

        // 3.获取主题业务枚举
        TopicEnum topicEnum = TopicEnum.match(requestTopic);
        if (topicEnum == null) {
            sendError(reqId, NONE_EXIST_TOPIC);
            return;
        }

        // 4. 业务处理
        try {
            MqttTopicHandler mqttTopicHandler = topicHandlers.get(topicEnum);
            if (mqttTopicHandler == null) {
                sendError(reqId, NONE_IMPL_TOPIC);
                return;
            }
            MqttContext mqttContext = new MqttContext(clientId, reqId, true);
            sendResponseByRequest(mqttContext, requestTopic, mqttTopicHandler.handle(mqttContext,
                    payloadObject.toJSONString()));
        } catch (Exception e) {
            log.warn("topic {} payload {} error:", requestTopic, payload, e);
            sendError(reqId, EXCEPTION);
        }
    }

    @Override
    public void broadcastMessage(@NotNull TopicEnum topicEnum, Object payload, Object... path) {
        String broadcastTopic = GLOBAL_PATH + topicEnum.getPath();
        if (path != null && path.length > 0) {
            broadcastTopic = GLOBAL_PATH + String.format(topicEnum.getPath(), path);
        }
        doSendResponse(broadcastTopic, MqttResp.success(brokerReqId.getAndIncrement(), payload), false);
    }

    @Override
    public void sendClientMessage(@NotNull TopicEnum topicEnum, @NotNull String clientId, Object payload) {
        String topic = GLOBAL_PATH + "/" + clientId + topicEnum.getPath();
        doSendResponse(topic, MqttResp.success(brokerReqId.getAndIncrement(), payload));
    }

    /**
     * 发送响应到客户端专属主题
     */
    private void sendResponseByRequest(MqttContext context, String requestTopic, MqttResp<?> payload) {
        if (!context.getSendRep()) {
            return;
        }
        String responseTopic = requestTopic.replace("req", "rep");
        if (payload == null) {
            doSendResponse(responseTopic, MqttResp.fail(context.getReqId(), EXCEPTION.getMsg()));
            return;
        }
        doSendResponse(responseTopic, payload);
    }

    private void sendError(Long reqId, MqttRespEnum respEnum) {
        String errorTopic = GLOBAL_PATH + TopicEnum.ERROR_TOPIC.getPath();
        MqttResp<String> resp = new MqttResp<>(reqId, respEnum);
        log.warn("Send error message: Topic={}, Payload={}", errorTopic, resp);
        doSendResponse(errorTopic, resp);
    }

    /**
     * 从请求主题中解析客户端ID
     */
    private String parseClientId(String topic) {
        Matcher matcher = TOPIC_PATTERN.matcher(topic);
        return matcher.matches() ? matcher.group("clientId") : null;
    }

    /**
     * 通用消息头获取
     */
    private String getHeader(Message<?> message) {
        Object value = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        return value != null ? value.toString() : null;
    }

    /**
     * 发送响应到客户端专属主题
     */
    private void doSendResponse(String responseTopic, MqttResp<?> payload) {
        doSendResponse(responseTopic, payload, true);
    }

    /**
     * 发送响应到客户端专属主题
     */
    private void doSendResponse(String responseTopic, MqttResp<?> payload, boolean
            withLog) {
        Message<String> message = MessageBuilder.withPayload(payload.toString())
                .setHeader(MqttHeaders.TOPIC, responseTopic).build();
        if (withLog) {
            log.info("send message to {}, data:{}", responseTopic, payload);
        }
        mqttOutboundChannel.send(message);
    }
}