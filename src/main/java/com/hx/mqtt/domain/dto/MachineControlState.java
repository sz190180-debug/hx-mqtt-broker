package com.hx.mqtt.domain.dto;

import lombok.Data;

/**
 * 机台控制状态类
 */
@Data
public class MachineControlState {
    private MachineControlStep currentStep = MachineControlStep.IDLE;
    private MachineControlMode mode = MachineControlMode.CALL_MATERIAL;
    private Long currentTaskId;
    private Long currentAmrId;
    private Long lastUpdateTime = System.currentTimeMillis();

    public void setCurrentStep(MachineControlStep currentStep) {
        this.currentStep = currentStep;
        this.lastUpdateTime = System.currentTimeMillis();
    }
}