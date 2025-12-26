package com.hx.mqtt.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.enums.TaskTypeEnum;
import com.hx.mqtt.config.MachineControlConfig;
import com.hx.mqtt.domain.dto.MachineControlMode;
import com.hx.mqtt.domain.dto.MachineControlState;
import com.hx.mqtt.domain.dto.MachineControlStep;
import com.hx.mqtt.domain.entity.MachineStation;
import com.hx.mqtt.domain.entity.MachineStationRegister;
import com.hx.mqtt.domain.rep.machine.MachineStationRep;
import com.hx.mqtt.domain.req.api.TaskChainAddReq;
import com.hx.mqtt.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.hx.mqtt.common.GlobalCache.CONTROL_STATE_MAP;
import static com.hx.mqtt.common.GlobalCache.MACHINE_TASK_MAP;

/**
 * 寄存器控制服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineControlServiceImpl implements MachineControlService {

    private final MachineStationService machineStationService;
    private final MachineStationRegisterService machineStationRegisterService;
    private final ModbusService modbusService;
    private final RcsApiService rcsApiService;
    private final MachineControlConfig machineControlConfig;

    @Override
    public void processMachineControlSignals() {
        try {
            // 获取所有启用的寄存器
            List<MachineStation> stations = machineStationService.getAllEnabledStations();
            if (CollectionUtils.isEmpty(stations)) {
                return;
            }
            List<MachineStationRegister> allEnableRegister = machineStationRegisterService.getAllEnableRegister(stations.stream().map(MachineStation::getStationId).collect(Collectors.toList())).stream().filter(v -> v.getRegisterType() == 1).collect(Collectors.toList());
            for (MachineStationRegister register : allEnableRegister) {
                try {
                    processSingleMachineControl(register);
                } catch (Exception e) {
                    log.error("处理寄存器{}控制信号失败", register.getRegisterId(), e);
                }
            }
        } catch (Exception e) {
            log.error("处理寄存器控制信号失败", e);
        }
    }

    @Override
    public void processSingleMachineControl(MachineStationRegister register) {
        MachineControlState state = CONTROL_STATE_MAP.computeIfAbsent(register.getRegisterId(), k -> new MachineControlState());

        // 根据当前状态执行相应的处理逻辑
        switch (state.getCurrentStep()) {
            case IDLE:
                handleIdleState(register, state);
                break;
            case MOVING_TO_REQUEST:
                handleMovingToRequestState(register, state);
                break;
            case REQUESTING_ENTER:
                handleRequestingEnterState(register, state);
                break;
            case LOADING_AND_MOVING:
                handleLoadingAndMovingState(register, state);
                break;
            case SENDING_EXIT_SIGNAL:
                handleSendingExitSignalState(register, state);
                break;
            case TRIGGERING_INBOUND:
                handleTriggeringInboundState(register, state);
                break;
        }
    }

    /**
     * 空闲态：检测控制寄存器
     * 1/3=叫车，2/4=叫料，按不同分支处理
     */
    private void handleIdleState(MachineStationRegister register, MachineControlState state) {
        Integer controlValue = readRegister(register);
        if (controlValue == null || !Objects.equals(controlValue, register.getControlValue())) {
            return;
        }
        log.info("读取寄存器{}控制寄存器为:{}", register.getRegisterId(), controlValue);
        Integer controlType = register.getControlType();
        Long taskId = null;
        if (controlType == 1 || controlType == 3) {
            taskId = triggerMoveToRequestPoint(register);
            if (taskId != null) {
                state.setMode(MachineControlMode.CALL_CAR);
                log.info("寄存器{}(叫车)移动到请求点位{}，任务ID: {}", register.getRegisterId(), register.getVertexCode(), taskId);
            }
        } else if (controlType == 2 || controlType == 4) {
            // 叫料分支：空托盘上料 -> 移动至请求点
            taskId = triggerLoadEmptyTrayAndMoveToRequest(register);
            if (taskId != null) {
                state.setMode(MachineControlMode.CALL_MATERIAL);
                log.info("寄存器{}(叫料)开始执行：空托盘上料并移动至请求点；任务ID: {}", register.getRegisterId(), taskId);
            }
        }
        if (taskId == null) {
            doOnError(state);
            return;
        }
        MACHINE_TASK_MAP.put(taskId, register.getRegisterId());
        state.setCurrentTaskId(taskId);
        state.setCurrentStep(MachineControlStep.MOVING_TO_REQUEST);
        clearRegisterSignal(register);
    }

    /**
     * 处理移动到请求点状态
     */
    private void handleMovingToRequestState(MachineStationRegister register, MachineControlState state) {
        // 检查移动任务是否完成
        if (isTaskCompleted(state.getCurrentTaskId())) {
            log.info("寄存器{}移动到请求点完成,清除当前任务id{}", register.getRegisterId(), state.getCurrentTaskId());
            state.setCurrentStep(MachineControlStep.REQUESTING_ENTER);
            state.setCurrentTaskId(null);
        } else {
            log.info("寄存器{}移动到请求点任务id{}未完成", register.getRegisterId(), state.getCurrentTaskId());
        }
    }

    /**
     * 处理请求进入状态
     */
    private void handleRequestingEnterState(MachineStationRegister register, MachineControlState state) {
        // 发送请求进入信号
        Long stationId = register.getStationId();
        if (sendRequestEnterSignal(register.getStationId())) {
            // 检查允许进入信号
            log.info("寄存器{}尝试写入请求进入信号", register.getRegisterId());
            if (checkAllowEnterSignal(stationId)) {
                log.info("寄存器{}收到允许进入信号", register.getRegisterId());
                state.setCurrentStep(MachineControlStep.LOADING_AND_MOVING);
                writeRegister(getRegisterByType(stationId, 2), 0);
                writeRegister(getRegisterByType(stationId, 3), 0);
            } else {
                log.info("寄存器{}未收到允许进入信号,不进行操作", register.getRegisterId());
            }
        }
    }

    /**
     * 处理上料/下料与移动阶段：
     * - 叫料：直接下料(O13) -> 移动至退出点
     * - 叫车：上料(O1) -> 移动至退出点
     */
    private void handleLoadingAndMovingState(MachineStationRegister register, MachineControlState state) {
        Long taskId;
        if (state.getMode() == MachineControlMode.CALL_MATERIAL) {
            // 叫料：直接下料 -> 移动退出
            taskId = triggerLoadingAndMoveToExit(register, state.getCurrentAmrId()); // 已调整为O13+移动
        } else {
            // 叫车：上料 -> 移动退出
            taskId = triggerPickupAndMoveToExit(register, state.getCurrentAmrId());  // 新方法，O1+移动
        }
        if (taskId != null) {
            MACHINE_TASK_MAP.put(taskId, register.getRegisterId());
            state.setCurrentTaskId(taskId);
            state.setCurrentStep(MachineControlStep.SENDING_EXIT_SIGNAL);
            log.info("寄存器{}开始{}并移动到退出点，任务ID: {}", register.getStationId(), state.getMode() == MachineControlMode.CALL_MATERIAL ? "直接下料" : "上料", taskId);
        } else {
            doOnError(state);
        }
    }

    /**
     * 处理发送退出信号状态
     */
    private void handleSendingExitSignalState(MachineStationRegister register, MachineControlState state) {
        // 检查上料和移动任务是否完成
        if (isTaskCompleted(state.getCurrentTaskId())) {
            if (state.getMode() == MachineControlMode.CALL_MATERIAL) {
                if (sendAgvExitCompleteSignal(register.getStationId(), 2)) {
                    log.info("CALL_MATERIAL寄存器{}发送AGV退出完成信号,清除任务链id{}", register.getStationId(), state.getCurrentTaskId());
                    state.setCurrentStep(MachineControlStep.TRIGGERING_INBOUND);
                    state.setCurrentTaskId(null);
                }
            } else {
                if (sendAgvExitCompleteSignal(register.getStationId(), 1)) {
                    log.info("CALL_CAR寄存器{}发送AGV退出完成信号,清除任务链id{}", register.getStationId(), state.getCurrentTaskId());
                    state.setCurrentStep(MachineControlStep.TRIGGERING_INBOUND);
                    state.setCurrentTaskId(null);
                }
            }
        } else {
            log.info("寄存器{}退出任务未完成", register.getStationId());
        }
    }

    /**
     * 处理触发入库状态
     */
    private void handleTriggeringInboundState(MachineStationRegister register, MachineControlState state) {
        // 触发入库任务链
        if (state.getMode() == MachineControlMode.CALL_CAR) {
            Long taskChainId = triggerInboundTaskChain(register);
            if (taskChainId != null) {
                log.info("寄存器{}触发入库任务链，任务链ID: {}", register.getRegisterId(), taskChainId);
            } else {
                log.error("寄存器{}触发入库任务链失败", register.getRegisterId());
            }
        }

        // 重置状态到空闲
        state.setCurrentStep(MachineControlStep.IDLE);
        state.setCurrentTaskId(null);
        state.setCurrentAmrId(null);
    }

    private Long triggerMoveToRequestPoint(MachineStationRegister register) {
        try {
            Long registerId = register.getRegisterId();
            if (register.getVertexCode() == null) {
                log.error("寄存器{}缺少空请求点位配置", registerId);
                return null;
            }
            MachineStationRegister requestReg = getRegisterByType(register.getStationId(), 2);
            MachineStationRep station = machineStationService.getMachineStationDetail(register.getStationId());
            if (station == null) {
                log.error("寄存器{}不存在", registerId);
                return null;
            }

            Long mapId = station.getMapId();
            if (mapId == null) {
                log.error("寄存器{}未配置地图ID", registerId);
                return null;
            }

            // 组装一个仅含单个移动任务的任务链下发
            TaskChainAddReq req = new TaskChainAddReq();
            TaskChainAddReq.TaskChain chain = new TaskChainAddReq.TaskChain();
            chain.setIsReturn(1);
            chain.setAreaId(station.getAreaId());
            req.setTaskChain(chain);

            TaskChainAddReq.TaskPo task = new TaskChainAddReq.TaskPo();
            task.setTaskType(TaskTypeEnum.O0.name()); // 移动任务
            task.setMapId(mapId);
            task.setEndPointCode(requestReg.getVertexCode());
            req.setTasks(Collections.singletonList(task));

            Integer taskId = rcsApiService.taskAdd(req);
            log.info("寄存器{}触发移动任务到点位{}，任务ID:{}", registerId, register.getVertexCode(), taskId);
            return taskId == null ? null : taskId.longValue();
        } catch (Exception e) {
            log.error("触发移动任务失败", e);
            return null;
        }
    }

    private boolean sendRequestEnterSignal(Long stationId) {
        try {
            MachineStationRegister requestRegister = getRegisterByType(stationId, 2); // 请求进入寄存器
            if (requestRegister == null) {
                log.error("机台{}未配置请求进入寄存器", stationId);
                return false;
            }

            return writeRegister(requestRegister, 1);
        } catch (Exception e) {
            log.error("发送请求进入信号失败", e);
            return false;
        }
    }

    /**
     * 触发到空托盘点位上料并移动至请求点
     */
    private Long triggerLoadEmptyTrayAndMoveToRequest(MachineStationRegister register) {
        try {
            Long registerId = register.getRegisterId();
            MachineStationRegister requestReg = getRegisterByType(register.getStationId(), 2);
            MachineStationRep station = machineStationService.getMachineStationDetail(register.getStationId());
            if (station == null) {
                log.error("寄存器{}不存在", registerId);
                return null;
            }
            Long mapId = station.getMapId();
            if (mapId == null) {
                log.error("寄存器{}未配置地图ID", registerId);
                return null;
            }

            TaskChainAddReq req = new TaskChainAddReq();
            TaskChainAddReq.TaskChain chain = new TaskChainAddReq.TaskChain();
            chain.setIsReturn(1);
            chain.setAreaId(station.getAreaId());
            req.setTaskChain(chain);

            List<TaskChainAddReq.TaskPo> tasks = new ArrayList<>();

            // 1) 空托盘点位上料
            TaskChainAddReq.TaskPo loadEmptyTray = new TaskChainAddReq.TaskPo();
            loadEmptyTray.setTaskType(TaskTypeEnum.O1.name()); // 上料
            loadEmptyTray.setMapId(mapId);
            loadEmptyTray.setEndPointCode(register.getCallVertexCode());
            tasks.add(loadEmptyTray);

            // 2) 移动至请求点
            TaskChainAddReq.TaskPo moveToRequest = new TaskChainAddReq.TaskPo();
            moveToRequest.setTaskType(TaskTypeEnum.O0.name()); // 移动
            moveToRequest.setMapId(mapId);
            moveToRequest.setEndPointCode(requestReg.getVertexCode());
            tasks.add(moveToRequest);

            req.setTasks(tasks);

            Integer taskIdInt = rcsApiService.taskAdd(req);
            if (taskIdInt == null) {
                log.error("寄存器{} 空托盘上料并移动至请求点 下发失败", registerId);
                return null;
            }
            Long taskId = taskIdInt.longValue();
            log.info("寄存器{} 下发完成：空托盘上料->移动至请求点，任务ID:{}", registerId, taskId);
            return taskId;
        } catch (Exception e) {
            log.error("触发空托盘上料并移动至请求点失败", e);
            return null;
        }
    }

    private boolean checkAllowEnterSignal(Long stationId) {
        try {
            MachineStationRegister allowRegister = getRegisterByType(stationId, 3); // 允许进入寄存器
            if (allowRegister == null) {
                log.error("机台{}未配置允许进入寄存器", stationId);
                return false;
            }

            Integer value = readRegister(allowRegister);

            return value != null && value == 1;
        } catch (Exception e) {
            log.error("检查允许进入信号失败", e);
            return false;
        }
    }

    private Long triggerLoadingAndMoveToExit(MachineStationRegister register, Long currentAmrId) {
        try {
            MachineStationRep station = machineStationService.getMachineStationDetail(register.getStationId());
            Long mapId = station.getMapId();
            if (mapId == null) {
                log.error("寄存器{}未配置地图ID", register.getRegisterId());
                return null;
            }

            MachineStationRegister exitVertex = getRegisterByType(register.getStationId(), 4); // 请求/对应点
            if (register.getVertexCode() == null || exitVertex == null || exitVertex.getVertexCode() == null) {
                log.error("机台{}缺少请求/退出点位配置", register.getStationId());
                return null;
            }

            TaskChainAddReq req = new TaskChainAddReq();
            TaskChainAddReq.TaskChain chain = new TaskChainAddReq.TaskChain();
            chain.setIsReturn(1);
            chain.setAreaId(station.getAreaId());
            chain.setAmrId(currentAmrId);
            req.setTaskChain(chain);

            List<TaskChainAddReq.TaskPo> tasks = new ArrayList<>();

            // 上料任务，目标为请求点
            TaskChainAddReq.TaskPo loadTask = new TaskChainAddReq.TaskPo();
            loadTask.setTaskType(TaskTypeEnum.O13.name());
            loadTask.setMapId(mapId);
            loadTask.setEndPointCode(register.getCallVertexCode());
            tasks.add(loadTask);

            // 移动到退出点
            TaskChainAddReq.TaskPo moveExit = new TaskChainAddReq.TaskPo();
            moveExit.setTaskType(TaskTypeEnum.O0.name());
            moveExit.setMapId(mapId);
            moveExit.setEndPointCode(exitVertex.getVertexCode());
            tasks.add(moveExit);

            req.setTasks(tasks);

            Integer taskIdInt = rcsApiService.taskAdd(req);
            if (taskIdInt == null) {
                log.error("寄存器{} 上料并移动到退出点 下发失败", register.getRegisterId());
                return null;
            }
            Long taskId = taskIdInt.longValue();
            log.info("寄存器{} 上料并移动到退出点 下发完成，任务ID:{}", register.getRegisterId(), taskId);
            return taskId;
        } catch (Exception e) {
            log.error("触发上料并移动到退出点失败", e);
            return null;
        }
    }

    /**
     * 叫车：上料(O1) -> 移动到退出点
     */
    private Long triggerPickupAndMoveToExit(MachineStationRegister register, Long currentAmrId) {
        try {
            MachineStationRep station = machineStationService.getMachineStationDetail(register.getStationId());
            Long mapId = station.getMapId();
            if (mapId == null) {
                log.error("寄存器{}未配置地图ID", register.getRegisterId());
                return null;
            }

            MachineStationRegister exitVertex = getRegisterByType(register.getStationId(), 4); // 请求/对应点
            if (register.getVertexCode() == null || exitVertex == null || exitVertex.getVertexCode() == null) {
                log.error("寄存器{}缺少请求/退出点位配置", register.getRegisterId());
                return null;
            }

            TaskChainAddReq req = new TaskChainAddReq();
            TaskChainAddReq.TaskChain chain = new TaskChainAddReq.TaskChain();
            chain.setIsReturn(1);
            chain.setAreaId(station.getAreaId());
            chain.setAmrId(currentAmrId);
            req.setTaskChain(chain);

            List<TaskChainAddReq.TaskPo> tasks = new ArrayList<>();

            // 上料任务，目标为请求点
            TaskChainAddReq.TaskPo loadTask = new TaskChainAddReq.TaskPo();
            loadTask.setTaskType(TaskTypeEnum.O1.name());
            loadTask.setMapId(mapId);
            loadTask.setEndPointCode(register.getVertexCode());
            tasks.add(loadTask);

            // 移动到退出点
            TaskChainAddReq.TaskPo moveExit = new TaskChainAddReq.TaskPo();
            moveExit.setTaskType(TaskTypeEnum.O0.name());
            moveExit.setMapId(mapId);
            moveExit.setEndPointCode(exitVertex.getVertexCode());
            tasks.add(moveExit);

            req.setTasks(tasks);

            Integer taskIdInt = rcsApiService.taskAdd(req);
            if (taskIdInt == null) {
                log.error("寄存器{} 上料并移动到退出点 下发失败", register.getStationId());
                return null;
            }
            Long taskId = taskIdInt.longValue();
            log.info("寄存器{} 上料并移动到退出点 下发完成，任务ID:{}", register.getStationId(), taskId);
            return taskId;
        } catch (Exception e) {
            log.error("触发上料并移动到退出点失败", e);
            return null;
        }
    }

    private boolean sendAgvExitCompleteSignal(Long registerId, int i) {
        try {
            MachineStationRegister exitRegister = getRegisterByType(registerId, 4); // AGV退出完成寄存器
            if (exitRegister == null) {
                log.error("寄存器{}未配置AGV退出完成寄存器", registerId);
                return false;
            }

            return writeRegister(exitRegister, i);
        } catch (Exception e) {
            log.error("发送AGV退出完成信号失败", e);
            return false;
        }
    }

    private Long triggerInboundTaskChain(MachineStationRegister register) {
        try {
            Long stationId = register.getStationId();
            MachineStationRep station = machineStationService.getMachineStationDetail(stationId);
            if (station == null || station.getTaskChainTemplateId() == null) {
                log.error("寄存器{}未配置任务链模板", stationId);
                return null;
            }

            // 使用模板触发任务链，一种常见方式是通过模板ID生成任务清单；
            // 如果已有专门的“模板提交”接口，可直接调用 rcsApiService.taskChainTemplateSubmit(templateId)
            Integer taskChainId = rcsApiService.taskChainTemplateSubmit(station.getTaskChainTemplateId());
            if (taskChainId == null) {
                log.error("寄存器{}触发模板{}失败", stationId, station.getTaskChainTemplateId());
                return null;
            }
            log.info("寄存器{}触发入库任务链成功，任务链ID: {}", stationId, taskChainId);
            return taskChainId.longValue();
        } catch (Exception e) {
            log.error("触发入库任务链失败", e);
            return null;
        }
    }

    private boolean clearRegisterSignal(MachineStationRegister register) {
        return writeRegister(register, 0);
    }

    /**
     * 读取控制寄存器
     */
    private Integer readRegister(MachineStationRegister register) {
        try {
            return modbusService.readSingleRegister(machineControlConfig.getModbus().getDefaultIp(), machineControlConfig.getModbus().getDefaultPort(), machineControlConfig.getModbus().getDefaultUnitId(), register.getRegisterAddress());
        } catch (Exception e) {
            log.error("读取控制寄存器失败", e);
            return null;
        }
    }

    /**
     * 读取控制寄存器
     */
    private boolean writeRegister(MachineStationRegister register, Integer value) {
        try {
            return modbusService.writeSingleRegister(machineControlConfig.getModbus().getDefaultIp(), machineControlConfig.getModbus().getDefaultPort(), machineControlConfig.getModbus().getDefaultUnitId(), register.getRegisterAddress(), value);
        } catch (Exception e) {
            log.error("读取控制寄存器失败", e);
            return false;
        }
    }

    /**
     * 根据类型获取寄存器配置
     */
    private MachineStationRegister getRegisterByType(Long registerId, Integer registerType) {
        LambdaQueryWrapper<MachineStationRegister> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MachineStationRegister::getStationId, registerId).eq(MachineStationRegister::getRegisterType, registerType).eq(MachineStationRegister::getStatus, 1).orderByDesc(MachineStationRegister::getCreatedTime); // 若多条，按创建时间倒序取最新
        return machineStationRegisterService.getOne(wrapper, false);
    }

    /**
     * 检查任务是否完成
     */
    private boolean isTaskCompleted(Long taskId) {
        if (taskId == null) {
            return false;
        }

        return !MACHINE_TASK_MAP.containsKey(taskId);
    }

    private void doOnError(MachineControlState state) {
        log.warn("{}下发任务失败", JSON.toJSONString(state));
        state.setMode(null);
        state.setCurrentStep(MachineControlStep.IDLE);
        state.setCurrentAmrId(null);
        state.setCurrentTaskId(null);
    }
}
