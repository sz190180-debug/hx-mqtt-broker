package com.hx.mqtt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.MachineStation;
import com.hx.mqtt.domain.rep.machine.MachineStationRep;
import com.hx.mqtt.domain.req.machine.MachineStationAddReq;
import com.hx.mqtt.domain.req.machine.MachineStationQueryReq;
import com.hx.mqtt.domain.req.machine.MachineStationUpdateReq;
import com.hx.mqtt.mapper.MachineStationMapper;
import com.hx.mqtt.service.MachineStationRegisterService;
import com.hx.mqtt.service.MachineStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 机台管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class MachineStationServiceImpl extends ServiceImpl<MachineStationMapper, MachineStation> implements MachineStationService {

    private final MachineStationRegisterService machineStationRegisterService;

    @Override
    public Long addMachineStation(MachineStationAddReq req) {
        MachineStation machineStation = new MachineStation();
        BeanUtil.copyProperties(req, machineStation);
        machineStation.setCreatedTime(new Date());
        machineStation.setUpdatedTime(new Date());
        
        save(machineStation);
        return machineStation.getStationId();
    }

    @Override
    public Boolean updateMachineStation(MachineStationUpdateReq req) {
        MachineStation machineStation = getById(req.getStationId());
        if (machineStation == null) {
            return false;
        }

        BeanUtil.copyProperties(req, machineStation);
        machineStation.setUpdatedTime(new Date());
        
        return updateById(machineStation);
    }

    @Override
    public Boolean deleteMachineStation(Long stationId) {
        // 检查是否存在关联的寄存器配置
        List<com.hx.mqtt.domain.rep.machine.MachineStationRegisterRep> registers = 
            machineStationRegisterService.getRegistersByStationId(stationId);
        if (!registers.isEmpty()) {
            throw new RuntimeException("该机台存在关联的寄存器配置，无法删除");
        }
        
        return removeById(stationId);
    }

    @Override
    public IPage<MachineStationRep> pageMachineStation(MachineStationQueryReq req) {
        LambdaQueryWrapper<MachineStation> wrapper = Wrappers.lambdaQuery();

        if (StrUtil.isNotBlank(req.getStationName())) {
            wrapper.like(MachineStation::getStationName, req.getStationName());
        }
        if (req.getDeviceType() != null) {
            wrapper.eq(MachineStation::getDeviceType, req.getDeviceType());
        }
        if (req.getMapId() != null) {
            wrapper.eq(MachineStation::getMapId, req.getMapId());
        }
        if (req.getAreaId() != null) {
            wrapper.eq(MachineStation::getAreaId, req.getAreaId());
        }
        if (req.getStatus() != null) {
            wrapper.eq(MachineStation::getStatus, req.getStatus());
        }

        wrapper.orderByAsc(MachineStation::getStationId);

        Page<MachineStation> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<MachineStation> result = page(page, wrapper);

        return result.convert(this::convertToRep);
    }

    @Override
    public MachineStationRep getMachineStationDetail(Long stationId) {
        MachineStation machineStation = getById(stationId);
        if (machineStation == null) {
            return null;
        }

        MachineStationRep rep = convertToRep(machineStation);
        rep.setRegisters(machineStationRegisterService.getRegistersByStationId(stationId));

        return rep;
    }

    @Override
    public List<MachineStationRep> getAllMachineStations() {
        List<MachineStation> stations = list();
        return stations.stream().map(this::convertToRep).collect(Collectors.toList());
    }

    @Override
    public List<MachineStationRep> getMachineStationsByMapId(Long mapId) {
        LambdaQueryWrapper<MachineStation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MachineStation::getMapId, mapId);
        wrapper.orderByAsc(MachineStation::getStationId);
        
        List<MachineStation> stations = list(wrapper);
        return stations.stream().map(this::convertToRep).collect(Collectors.toList());
    }

    @Override
    public List<MachineStationRep> getMachineStationsByAreaId(Long areaId) {
        LambdaQueryWrapper<MachineStation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MachineStation::getAreaId, areaId);
        wrapper.orderByAsc(MachineStation::getStationId);

        List<MachineStation> stations = list(wrapper);
        return stations.stream().map(this::convertToRep).collect(Collectors.toList());
    }

    @Override
    public List<MachineStation> getAllEnabledStations() {
        LambdaQueryWrapper<MachineStation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MachineStation::getStatus, 1); // 只查询启用的机台

        return list(wrapper);
    }

    /**
     * 转换为响应对象
     */
    private MachineStationRep convertToRep(MachineStation machineStation) {
        MachineStationRep rep = new MachineStationRep();
        BeanUtil.copyProperties(machineStation, rep);
        
        // 设置设备类型描述
        if (machineStation.getDeviceType() != null) {
            switch (machineStation.getDeviceType()) {
                case 1:
                    rep.setDeviceTypeDesc("机械手");
                    break;
                default:
                    rep.setDeviceTypeDesc("未知");
                    break;
            }
        }
        
        // 设置状态描述
        if (machineStation.getStatus() != null) {
            rep.setStatusDesc(machineStation.getStatus() == 1 ? "启用" : "禁用");
        }
        
        return rep;
    }
}
