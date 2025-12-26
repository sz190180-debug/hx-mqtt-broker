package com.hx.mqtt.service;

import com.hx.mqtt.domain.entity.MachineStationRegister;

/**
 * 机台控制服务接口
 */
public interface MachineControlService {

    /**
     * 处理机台控制信号
     */
    void processMachineControlSignals();

    /**
     * 处理单个机台的控制流程
     *
     */
    void processSingleMachineControl(MachineStationRegister register);
}
