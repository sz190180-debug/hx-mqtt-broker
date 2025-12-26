package com.hx.mqtt.config;

import com.alibaba.fastjson2.JSONObject;
import com.hx.mqtt.common.MqttClientManager;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttBrokerInterceptor extends AbstractInterceptHandler {

    @Override
    public String getID() {
        return "";
    }

    @Override
    public void onConnect(InterceptConnectMessage msg) {
        String clientId = msg.getClientID();
        log.info("客户端连接:{}", JSONObject.toJSONString(msg));
        MqttClientManager.addClient(clientId); // 存储客户端
    }

    @Override
    public void onDisconnect(InterceptDisconnectMessage msg) {
        String clientId = msg.getClientID();
        log.info("客户端{}断开,clientId:{}", msg.getUsername(), clientId);
        MqttClientManager.removeClient(clientId); // 移除客户端
    }

}