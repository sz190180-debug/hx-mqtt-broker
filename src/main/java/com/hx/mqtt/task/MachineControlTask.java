package com.hx.mqtt.task;

import com.hx.mqtt.service.MachineControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 机台控制定时任务
 * 定期读取机台控制寄存器，处理叫车信号和控制流程
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "machine.control.enabled", havingValue = "true", matchIfMissing = true)
public class MachineControlTask {

    private final MachineControlService machineControlService;

    /**
     * 机台控制信号处理定时任务
     * 周期从application.yml读取：machine.control.schedule.control-signal-interval
     */
    @Scheduled(fixedRateString = "${machine.control.schedule.control-signal-interval:5000}")
    public void processMachineControlSignals() {
        try {
            log.debug("开始处理机台控制信号");
            machineControlService.processMachineControlSignals();
            log.debug("机台控制信号处理完成");
        } catch (Exception e) {
            log.error("机台控制信号处理异常", e);
        }
    }
}
