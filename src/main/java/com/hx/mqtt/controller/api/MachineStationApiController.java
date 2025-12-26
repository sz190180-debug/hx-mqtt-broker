package com.hx.mqtt.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hx.mqtt.common.GlobalCache;
import com.hx.mqtt.common.HttpResp;
import com.hx.mqtt.domain.dto.MachineControlState;
import com.hx.mqtt.domain.entity.HxMap;
import com.hx.mqtt.domain.entity.HxMapVertexes;
import com.hx.mqtt.domain.rep.common.SelectOption;
import com.hx.mqtt.domain.rep.machine.MachineStationRegisterRep;
import com.hx.mqtt.domain.rep.machine.MachineStationRep;
import com.hx.mqtt.domain.req.machine.*;
import com.hx.mqtt.service.HxMapService;
import com.hx.mqtt.service.HxMapVertexesService;
import com.hx.mqtt.service.MachineStationRegisterService;
import com.hx.mqtt.service.MachineStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 机台管理API控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/machine/station")
public class MachineStationApiController {

    private final MachineStationService machineStationService;
    private final MachineStationRegisterService machineStationRegisterService;
    private final HxMapService hxMapService;
    private final HxMapVertexesService hxMapVertexesService;

    /**
     * 添加机台
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/add")
    public HttpResp<?> addMachineStation(@Valid @RequestBody MachineStationAddReq req) {
        try {
            Long stationId = machineStationService.addMachineStation(req);
            return HttpResp.success(stationId);
        } catch (RuntimeException e) {
            return HttpResp.error(e.getMessage());
        }
    }

    /**
     * 更新机台
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    public HttpResp<?> updateMachineStation(@Valid @RequestBody MachineStationUpdateReq req) {
        try {
            Boolean result = machineStationService.updateMachineStation(req);
            if (!result) {
                throw new RuntimeException("机台不存在或更新失败");
            }
            return HttpResp.success();
        } catch (RuntimeException e) {
            return HttpResp.error(e.getMessage());
        }
    }

    /**
     * 删除机台
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/delete/{stationId}")
    public HttpResp<?> deleteMachineStation(@PathVariable Long stationId) {
        try {
            Boolean result = machineStationService.deleteMachineStation(stationId);
            if (!result) {
                throw new RuntimeException("机台不存在或删除失败");
            }
            return HttpResp.success();
        } catch (RuntimeException e) {
            return HttpResp.error(e.getMessage());
        }
    }

    /**
     * 分页查询机台
     */
    @PostMapping("/page")
    public HttpResp<IPage<MachineStationRep>> pageMachineStation(@RequestBody MachineStationQueryReq req) {
        IPage<MachineStationRep> result = machineStationService.pageMachineStation(req);
        return HttpResp.success(result);
    }

    /**
     * 获取机台详情
     */
    @GetMapping("/detail/{stationId}")
    public HttpResp<MachineStationRep> getMachineStationDetail(@PathVariable Long stationId) {
        MachineStationRep result = machineStationService.getMachineStationDetail(stationId);
        if (result == null) {
            throw new RuntimeException("机台不存在");
        }
        return HttpResp.success(result);
    }

    /**
     * 获取所有机台
     */
    @GetMapping("/list")
    public HttpResp<List<MachineStationRep>> getAllMachineStations() {
        List<MachineStationRep> result = machineStationService.getAllMachineStations();
        return HttpResp.success(result);
    }

    /**
     * 根据地图ID获取机台列表
     */
    @GetMapping("/list/map/{mapId}")
    public HttpResp<List<MachineStationRep>> getMachineStationsByMapId(@PathVariable Long mapId) {
        List<MachineStationRep> result = machineStationService.getMachineStationsByMapId(mapId);
        return HttpResp.success(result);
    }

    /**
     * 根据区域ID获取机台列表
     */
    @GetMapping("/list/area/{areaId}")
    public HttpResp<List<MachineStationRep>> getMachineStationsByAreaId(@PathVariable Long areaId) {
        List<MachineStationRep> result = machineStationService.getMachineStationsByAreaId(areaId);
        return HttpResp.success(result);
    }

    // ==================== 机台寄存器配置管理 ====================

    /**
     * 添加机台寄存器配置
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/register/add")
    public HttpResp<?> addMachineStationRegister(@Valid @RequestBody MachineStationRegisterAddReq req) {
        try {
            Long registerId = machineStationRegisterService.addMachineStationRegister(req);
            return HttpResp.success(registerId);
        } catch (RuntimeException e) {
            return HttpResp.error(e.getMessage());
        }
    }

    /**
     * 更新机台寄存器配置
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/register/update")
    public HttpResp<?> updateMachineStationRegister(@Valid @RequestBody MachineStationRegisterUpdateReq req) {
        try {
            Boolean result = machineStationRegisterService.updateMachineStationRegister(req);
            if (!result) {
                return HttpResp.error("寄存器配置不存在或更新失败");
            }
            return HttpResp.success();
        } catch (RuntimeException e) {
            return HttpResp.error(e.getMessage());
        }
    }

    /**
     * 删除机台寄存器配置
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/register/delete/{registerId}")
    public HttpResp<?> deleteMachineStationRegister(@PathVariable Long registerId) {
        try {
            Boolean result = machineStationRegisterService.deleteMachineStationRegister(registerId);
            if (!result) {
                throw new RuntimeException("寄存器配置不存在或删除失败");
            }
            return HttpResp.success();
        } catch (RuntimeException e) {
            return HttpResp.error(e.getMessage());
        }
    }

    /**
     * 重置机台寄存器任务
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/register/cancel/{registerId}")
    public HttpResp<?> cancelMachineStationRegister(@PathVariable Long registerId) {
        try {
            MachineControlState state = GlobalCache.CONTROL_STATE_MAP.remove(registerId);
            if(state.getCurrentTaskId()!= null) {
                log.info("重置机台寄存器任务接口移除任务id{}",state.getCurrentTaskId());
                GlobalCache.MACHINE_TASK_MAP.remove(state.getCurrentTaskId());
            }
            return HttpResp.success();
        } catch (RuntimeException e) {
            return HttpResp.error(e.getMessage());
        }
    }

    /**
     * 分页查询机台寄存器配置
     */
    @PostMapping("/register/page")
    public HttpResp<IPage<MachineStationRegisterRep>> pageMachineStationRegister(@RequestBody MachineStationRegisterQueryReq req) {
        IPage<MachineStationRegisterRep> result = machineStationRegisterService.pageMachineStationRegister(req);
        return HttpResp.success(result);
    }

    /**
     * 获取机台寄存器配置详情
     */
    @GetMapping("/register/detail/{registerId}")
    public HttpResp<MachineStationRegisterRep> getMachineStationRegisterDetail(@PathVariable Long registerId) {
        MachineStationRegisterRep result = machineStationRegisterService.getMachineStationRegisterDetail(registerId);
        if (result == null) {
            throw new RuntimeException("寄存器配置不存在");
        }
        return HttpResp.success(result);
    }

    /**
     * 根据机台ID获取寄存器配置列表
     */
    @GetMapping("/register/list/station/{stationId}")
    public HttpResp<List<MachineStationRegisterRep>> getRegistersByStationId(@PathVariable Long stationId) {
        List<MachineStationRegisterRep> result = machineStationRegisterService.getRegistersByStationId(stationId);
        return HttpResp.success(result);
    }

    /**
     * 根据点位编码获取寄存器配置
     */
    @GetMapping("/register/list/vertex/{vertexCode}")
    public HttpResp<List<MachineStationRegisterRep>> getRegistersByVertexCode(@PathVariable String vertexCode) {
        List<MachineStationRegisterRep> result = machineStationRegisterService.getRegistersByVertexCode(vertexCode);
        return HttpResp.success(result);
    }

    // ==================== 页面适配接口 ====================

    /**
     * 获取地图列表
     */
    @GetMapping("/maps")
    public HttpResp<List<Map<String, Object>>> getMapList() {
        List<HxMap> maps = hxMapService.list();
        List<Map<String, Object>> result = maps.stream().map(map -> {
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("mapId", map.getMapId());
            mapData.put("mapName", "地图" + map.getMapId()); // 只显示地图ID
            return mapData;
        }).collect(Collectors.toList());
        return HttpResp.success(result);
    }

    /**
     * 根据地图ID获取区域列表（基于hx_map_vertexes表分组查询）
     */
    @GetMapping("/areas/{mapId}")
    public HttpResp<List<Map<String, Object>>> getAreasByMapId(@PathVariable Long mapId) {
        LambdaQueryWrapper<HxMapVertexes> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(HxMapVertexes::getMapId, mapId);
        wrapper.isNotNull(HxMapVertexes::getAreaId);
        wrapper.groupBy(HxMapVertexes::getAreaId);
        wrapper.select(HxMapVertexes::getAreaId);

        List<HxMapVertexes> vertexes = hxMapVertexesService.list(wrapper);
        List<Map<String, Object>> result = vertexes.stream().map(vertex -> {
            Map<String, Object> areaData = new HashMap<>();
            areaData.put("areaId", vertex.getAreaId());
            areaData.put("areaName", "区域" + vertex.getAreaId());
            return areaData;
        }).collect(Collectors.toList());
        return HttpResp.success(result);
    }

    /**
     * 获取机台下拉选项（用于页面选择器）
     */
    @GetMapping("/select")
    public HttpResp<List<MachineStationRep>> selectMachineStations() {
        List<MachineStationRep> result = machineStationService.getAllMachineStations();
        return HttpResp.success(result);
    }

    /**
     * 根据条件获取机台选项（支持搜索）
     */
    @PostMapping("/select")
    public HttpResp<List<MachineStationRep>> selectMachineStations(@RequestBody MachineStationQueryReq req) {
        // 设置较大的页面大小以获取所有匹配的记录
        req.setPageSize(1000L);
        IPage<MachineStationRep> result = machineStationService.pageMachineStation(req);
        return HttpResp.success(result.getRecords());
    }

    /**
     * 获取设备类型选项
     */
    @GetMapping("/deviceTypes")
    public HttpResp<List<SelectOption>> getDeviceTypes() {
        List<SelectOption> deviceTypes = Collections.singletonList(
                new SelectOption(1, "机械手")
        );
        return HttpResp.success(deviceTypes);
    }

    /**
     * 获取寄存器类型选项
     */
    @GetMapping("/register/types")
    public HttpResp<List<SelectOption>> getRegisterTypes() {
        List<SelectOption> registerTypes = Arrays.asList(
                new SelectOption(1, "CONTROL"),
                new SelectOption(2, "REQUEST_ENTER"),
                new SelectOption(3, "ALLOW_ENTER"),
                new SelectOption(4, "AGV_EXIT")
        );
        return HttpResp.success(registerTypes);
    }

    /**
     * 获取协议类型选项
     */
    @GetMapping("/register/protocols")
    public HttpResp<List<SelectOption>> getProtocolTypes() {
        List<SelectOption> protocolTypes = Collections.singletonList(
                new SelectOption("MODBUS_TCP", "MODBUS_TCP")
        );
        return HttpResp.success(protocolTypes);
    }

    /**
     * 获取数据类型选项
     */
    @GetMapping("/register/dataTypes")
    public HttpResp<List<SelectOption>> getDataTypes() {
        List<SelectOption> dataTypes = Arrays.asList(
                new SelectOption("BOOL", "BOOL"),
                new SelectOption("INT16", "INT16"),
                new SelectOption("INT32", "INT32"),
                new SelectOption("FLOAT", "FLOAT")
        );
        return HttpResp.success(dataTypes);
    }

    /**
     * 获取状态选项
     */
    @GetMapping("/statusOptions")
    public HttpResp<List<SelectOption>> getStatusOptions() {
        List<SelectOption> statusOptions = Arrays.asList(
                new SelectOption(1, "启用"),
                new SelectOption(0, "禁用")
        );
        return HttpResp.success(statusOptions);
    }

    // ==================== 批量操作接口 ====================

    /**
     * 批量删除机台
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/batch/delete")
    public HttpResp<Void> batchDeleteMachineStations(@RequestBody List<Long> stationIds) {
        for (Long stationId : stationIds) {
            machineStationService.deleteMachineStation(stationId);
        }
        return HttpResp.success();
    }

    /**
     * 批量更新机台状态
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/batch/updateStatus")
    public HttpResp<Void> batchUpdateMachineStationStatus(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> stationIds = (List<Long>) params.get("stationIds");
        Integer status = (Integer) params.get("status");

        for (Long stationId : stationIds) {
            MachineStationUpdateReq req = new MachineStationUpdateReq();
            req.setStationId(stationId);

            // 获取现有数据
            MachineStationRep existing = machineStationService.getMachineStationDetail(stationId);
            if (existing != null) {
                req.setStationName(existing.getStationName());
                req.setDeviceType(existing.getDeviceType());
                req.setMapId(existing.getMapId());
                req.setAreaId(existing.getAreaId());
                req.setDescription(existing.getDescription());
                req.setTaskChainTemplateId(existing.getTaskChainTemplateId());
                req.setFirstAddressId(existing.getFirstAddressId());
                req.setStatus(status);

                machineStationService.updateMachineStation(req);
            }
        }
        return HttpResp.success();
    }

    /**
     * 批量删除寄存器配置
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/register/batch/delete")
    public HttpResp<Void> batchDeleteMachineStationRegisters(@RequestBody List<Long> registerIds) {
        for (Long registerId : registerIds) {
            machineStationRegisterService.deleteMachineStationRegister(registerId);
        }
        return HttpResp.success();
    }
}
