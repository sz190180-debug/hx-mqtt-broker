package com.hx.mqtt;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.GlobalCache;
import com.hx.mqtt.config.DatabaseAuthenticator;
import com.hx.mqtt.domain.entity.HxRcsIp;
import com.hx.mqtt.mapper.HxRcsIpMapper;
import com.hx.mqtt.service.HxUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@EnableScheduling // 启用定时任务
@SpringBootApplication
@RequiredArgsConstructor
public class HxMqttApplication {

    private final HxRcsIpMapper hxRcsIpMapper;
    private final HxUserService hxUserService;

    public static void main(String[] args) {
        SpringApplication.run(HxMqttApplication.class, args);
    }

    @PostConstruct
    public void init() {
        DatabaseAuthenticator.hxUserService = hxUserService;
        try {
            HxRcsIp hxRcsIp = hxRcsIpMapper.selectOne(Wrappers.lambdaQuery());
            GlobalCache.RCS_IP.set(hxRcsIp);
        } catch (Exception ignored) {
        }
    }
}