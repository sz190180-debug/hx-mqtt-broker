package com.hx.mqtt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 机台控制配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "machine.control")
public class MachineControlConfig {

    /**
     * 是否启用机台控制功能
     */
    private boolean enabled = true;

    /**
     * Modbus配置
     */
    private ModbusConfig modbus = new ModbusConfig();

    /**
     * 定时任务配置
     */
    private ScheduleConfig schedule = new ScheduleConfig();

    @Data
    public static class ModbusConfig {
        /**
         * 默认IP地址
         */
        private String defaultIp = "192.168.1.100";

        /**
         * 默认端口
         */
        private int defaultPort = 502;

        /**
         * 默认单元ID
         */
        private int defaultUnitId = 1;

        /**
         * 连接超时时间（毫秒）
         */
        private int connectTimeout = 3000;

        /**
         * 读取超时时间（毫秒）
         */
        private int readTimeout = 5000;

        /**
         * 连接池最大大小
         */
        private int maxConnections = 50;
    }

    @Data
    public static class ScheduleConfig {
        /**
         * 控制信号检测间隔（毫秒）
         */
        private long controlSignalInterval = 5000;

        /**
         * 状态清理间隔（毫秒）
         */
        private long stateCleanupInterval = 60000;

        /**
         * 状态超时时间（毫秒）
         */
        private long stateTimeout = 300000; // 5分钟
    }
}
