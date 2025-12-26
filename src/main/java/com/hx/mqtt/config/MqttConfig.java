package com.hx.mqtt.config;

import com.hx.mqtt.common.MqttClientManager;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.io.IOException;
import java.util.Properties;

@Slf4j
@Configuration
public class MqttConfig {

    @Value("${server.ws-port:8089}")
    private String wsPort;

    @Value("${mqtt.broker.port}")
    private String brokerPort;  // 注入Broker端口

    @Value("${mqtt.broker.host}")
    private String brokerHost;  // 注入Broker监听地址

    @Value("${mqtt.client.server-uri}")
    private String clientServerUri;  // 注入客户端连接的URI

    @Value("${mqtt.client.producer-id:serverProducer}")
    private String clientProducerId;  // 注入生产者ID

    @Value("${mqtt.client.async}")
    private boolean clientAsync;  // 注入异步发送配置

    /**
     * 嵌入式Broker配置
     */
    @Bean
    public Server mqttBroker() throws IOException {
        Server server = new Server();
        IConfig config = new MemoryConfig(new Properties());
        config.setProperty("port", brokerPort);
        config.setProperty("host", brokerHost);
        // WebSocket配置
        config.setProperty("websocket_port", wsPort);
        config.setProperty("websocket_host", brokerHost);
        config.setProperty("allow_anonymous", "false");
        config.setProperty("authenticator_class", DatabaseAuthenticator.class.getName());


        server.startServer(config);
        // 注册拦截器
        server.addInterceptHandler(new MqttBrokerInterceptor());

        Runtime.getRuntime().addShutdownHook(new Thread(server::stopServer));
        return server;
    }

    /**
     * 出站消息处理器
     *
     * @param factory 工厂
     * @return 消息处理器
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(MqttPahoClientFactory factory) {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(clientProducerId, factory);
        handler.setAsync(clientAsync);
        return handler;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound(MqttPahoClientFactory factory) {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientServerUri,
                "serverInbound", factory, "/iot/+/req/#");
        adapter.setCompletionTimeout(5000);
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInboundChannel());
        return adapter;
    }

    /**
     * MQTT客户端工厂
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory() {
            @Override
            public IMqttAsyncClient getAsyncClientInstance(String serverUri, String clientId) throws MqttException {
                IMqttAsyncClient client = super.getAsyncClientInstance(serverUri, clientId);
                client.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        log.info("MQTT 连接成功: {}", clientId);
                        MqttClientManager.addClient(clientId);
                        try {
                            client.subscribe("/iot/+/req/#", 1); // 订阅所有请求主题
                        } catch (MqttException e) {
                            log.error("订阅失败", e);
                        }
                    }

                    @Override
                    public void connectionLost(Throwable cause) {
                        log.info("MQTT 连接断开: {}", clientId);
                        MqttClientManager.removeClient(clientId);
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        // 处理消息
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        // 消息发送完成
                    }
                });
                return client;
            }
        };

        MqttConnectOptions options = new MqttConnectOptions();
        // 支持WebSocket的URI格式
        // 构建WebSocket URI
        String wsUri = "ws://" + brokerHost + ":" + wsPort + "/mqtt";

        // 同时支持TCP和WebSocket（按顺序尝试）
        options.setServerURIs(new String[]{
                clientServerUri,  // TCP连接
                wsUri            // WebSocket连接
        });
        options.setUserName("");
        options.setPassword("".toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }
}