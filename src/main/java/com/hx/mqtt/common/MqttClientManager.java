package com.hx.mqtt.common;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MqttClientManager {
    // 存储 clientId -> MQTT 客户端信息（可以是自定义对象）
    private static final ConcurrentHashMap<String, Boolean> connectedClients = new ConcurrentHashMap<>();
    /**
     * 防止重复,应答时移除
     */
    private static final Map<String, Set<Long>> reqIdSet = new ConcurrentHashMap<>();

    public static void addClient(String clientId) {
        connectedClients.put(clientId, false);
    }

    public static boolean isOnline(String clientId) {
        return connectedClients.containsKey(clientId);
    }

    public static void removeClient(String clientId) {
        connectedClients.remove(clientId);
        reqIdSet.remove(clientId);
    }

    public static void authClient(String clientId) {
        if (!connectedClients.containsKey(clientId)) {
            throw new IllegalArgumentException(String.format("client id %s not exist", clientId));
        }
        connectedClients.put(clientId, true);
    }

    public static Boolean isAuth(String clientId) {
        return connectedClients.containsKey(clientId) && connectedClients.get(clientId);
    }

    public static Boolean addReqId(String clientId, Long reqId) {
        //防止弱网环境重复提交
        Set<Long> set = reqIdSet.getOrDefault(clientId, new HashSet<>());
        reqIdSet.put(clientId, set);
        return set.add(reqId);
    }

    public static int getConnectedCount() {
        return connectedClients.size();
    }
}