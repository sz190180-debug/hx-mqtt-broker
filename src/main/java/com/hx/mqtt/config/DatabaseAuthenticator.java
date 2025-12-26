// DatabaseAuthenticator.java
package com.hx.mqtt.config;

import com.hx.mqtt.domain.entity.HxUser;
import com.hx.mqtt.service.HxUserService;
import io.moquette.broker.security.IAuthenticator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseAuthenticator implements IAuthenticator {

    public static HxUserService hxUserService;

    @Override
    public boolean checkValid(String clientId, String username, byte[] passwordBytes) {
        // 1. 允许生产者客户端直通
        if ("serverInbound".equals(clientId) || "serverProducer".equals(clientId)) {
            return true;
        }

        // 2. 校验普通客户端
        HxUser user = hxUserService.getByClientId(clientId);
        if (user == null) {
            log.warn("客户端ID未注册: {}", clientId);
            return false;
        }

        // 3. 密码比对（建议使用加密验证）
        return user.getUsername().equals(username) && user.getPassword().equals(new String(passwordBytes));
    }
}