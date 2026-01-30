package com.hx.mqtt.common;

import com.hx.mqtt.domain.dto.AmrDataDto;
import com.hx.mqtt.domain.dto.MachineControlState;
import com.hx.mqtt.domain.entity.HxRcsIp;
import com.hx.mqtt.domain.req.api.TaskChainInfoReq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import com.hx.mqtt.domain.req.mqtt.TaskChainTemplateSubmitReq;

public class GlobalCache {

    public static final  AtomicReference<HxRcsIp> RCS_IP = new AtomicReference<>();

    /**
     * key:taskId value:clientId
     */
    public static final  Map<Long, String> TASK_CLIENT_ID_MAP = new ConcurrentHashMap<>();

    public static final  Map<Long, Long> TASK_ID_AMR_MAP = new ConcurrentHashMap<>();

    public static final  Map<Long, TaskChainInfoReq> TASK_WEIGHING_MAP = new ConcurrentHashMap<>();

    /**
     * key:taskId value:templateId
     */
    public static final  Map<Long, Long> TASK_ID_TEMPLATE_MAP = new ConcurrentHashMap<>();

    public static final  Map<Long, AmrDataDto> AMR_TABLE_MAP = new ConcurrentHashMap<>();

    /**
     * key:taskId value:registerId
     */
    public static final Map<Long, Long> MACHINE_TASK_MAP = new ConcurrentHashMap<>();

    /**
     * key:registerId value对象
     */
    public static final Map<Long, MachineControlState> CONTROL_STATE_MAP =
            new ConcurrentHashMap<>();

    /**
     * 全局重量缓存 (单位: kg)
     * 默认为 0
     */
    private static final AtomicReference<Double> currentWeight = new AtomicReference<>(0.0);


    public static final Map<Long, Double> AMR_WEIGHT_MAP = new ConcurrentHashMap<>();

    /**
     * 获取当前重量
     */
    public static Double getCurrentWeight() {
        return currentWeight.get();
    }

    /**
     * 设置当前重量
     */
    public static void setCurrentWeight(Double weight) {
        currentWeight.set(weight);
    }

    public static void removeByStatus(Integer status, Long taskId) {
        if (status != null && (status == 3 || status == 5 || status == 6 || status == 7)) {
            TASK_CLIENT_ID_MAP.remove(taskId);
            TASK_ID_AMR_MAP.remove(taskId);
        }
    }
}
