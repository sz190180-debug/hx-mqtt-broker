package com.hx.mqtt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hx.mqtt.domain.entity.MachineStationRegister;
import com.hx.mqtt.domain.rep.machine.MachineStationRegisterRep;
import com.hx.mqtt.domain.req.machine.MachineStationRegisterAddReq;
import com.hx.mqtt.domain.req.machine.MachineStationRegisterQueryReq;
import com.hx.mqtt.domain.req.machine.MachineStationRegisterUpdateReq;
import com.hx.mqtt.mapper.MachineStationRegisterMapper;
import com.hx.mqtt.service.MachineStationRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 机台寄存器配置服务实现类
 */
@Service
@RequiredArgsConstructor
public class MachineStationRegisterServiceImpl extends ServiceImpl<MachineStationRegisterMapper,
        MachineStationRegister> implements MachineStationRegisterService {

    @Override
    public Long addMachineStationRegister(MachineStationRegisterAddReq req) {
        // 检查同一机台下寄存器地址是否重复
        LambdaQueryWrapper<MachineStationRegister> addressCheckWrapper = Wrappers.lambdaQuery();
        addressCheckWrapper.eq(MachineStationRegister::getStationId, req.getStationId())
                .eq(MachineStationRegister::getRegisterAddress, req.getRegisterAddress())
                .eq(req.getControlType() != null, MachineStationRegister::getControlType, req.getControlType());

        if (count(addressCheckWrapper) > 0) {
            throw new RuntimeException("该机台下寄存器地址已存在");
        }

        if (req.getRegisterType() != null) {
            LambdaQueryWrapper<MachineStationRegister> controlCheckWrapper = Wrappers.lambdaQuery();
            controlCheckWrapper.eq(MachineStationRegister::getStationId, req.getStationId())
                    .eq(req.getControlType() != null, MachineStationRegister::getControlType, req.getControlType())
                    .eq(MachineStationRegister::getRegisterType, req.getRegisterType());

            if (count(controlCheckWrapper) > 0) {
                throw new RuntimeException("该机台已存在该类型寄存器，每个机台只能配置一个相同寄存器");
            }
        }

        MachineStationRegister register = new MachineStationRegister();
        BeanUtil.copyProperties(req, register);
        register.setCreatedTime(new Date());
        register.setUpdatedTime(new Date());

        save(register);
        return register.getRegisterId();
    }

    @Override
    public Boolean updateMachineStationRegister(MachineStationRegisterUpdateReq req) {
        MachineStationRegister register = getById(req.getRegisterId());
        if (register == null) {
            return false;
        }

        // 检查同一机台下寄存器地址是否重复（排除自己）
        LambdaQueryWrapper<MachineStationRegister> addressCheckWrapper = Wrappers.lambdaQuery();
        addressCheckWrapper.eq(MachineStationRegister::getStationId, req.getStationId())
                .eq(MachineStationRegister::getRegisterAddress, req.getRegisterAddress())
                .ne(MachineStationRegister::getRegisterId, req.getRegisterId())
                .eq(req.getControlType() != null, MachineStationRegister::getControlType, req.getControlType());

        if (count(addressCheckWrapper) > 0) {
            throw new RuntimeException("该机台下寄存器地址已存在");
        }

        // 检查控制寄存器唯一性：每个机台只能有一个控制寄存器（排除自己）
        if (req.getRegisterType() != null) {
            LambdaQueryWrapper<MachineStationRegister> controlCheckWrapper = Wrappers.lambdaQuery();
            controlCheckWrapper.eq(MachineStationRegister::getStationId, req.getStationId())
                    .eq(MachineStationRegister::getRegisterType, req.getRegisterType())
                    .eq(req.getControlType() != null, MachineStationRegister::getControlType, req.getControlType())
                    .ne(MachineStationRegister::getRegisterId, req.getRegisterId());

            if (count(controlCheckWrapper) > 0) {
                throw new RuntimeException("该机台已存在该类型寄存器，每个机台只能配置一个相同寄存器");
            }
        }

        BeanUtil.copyProperties(req, register);
        register.setUpdatedTime(new Date());

        return updateById(register);
    }

    @Override
    public Boolean deleteMachineStationRegister(Long registerId) {
        return removeById(registerId);
    }

    @Override
    public IPage<MachineStationRegisterRep> pageMachineStationRegister(MachineStationRegisterQueryReq req) {
        LambdaQueryWrapper<MachineStationRegister> wrapper = Wrappers.lambdaQuery();

        if (req.getStationId() != null) {
            wrapper.eq(MachineStationRegister::getStationId, req.getStationId());
        }
        if (req.getRegisterType() != null) {
            wrapper.eq(MachineStationRegister::getRegisterType, req.getRegisterType());
        }
        if (StrUtil.isNotBlank(req.getProtocolType())) {
            wrapper.eq(MachineStationRegister::getProtocolType, req.getProtocolType());
        }
        if (req.getStatus() != null) {
            wrapper.eq(MachineStationRegister::getStatus, req.getStatus());
        }
        if (StrUtil.isNotBlank(req.getVertexCode())) {
            wrapper.like(MachineStationRegister::getVertexCode, req.getVertexCode());
        }

        wrapper.orderByAsc(MachineStationRegister::getStationId, MachineStationRegister::getRegisterAddress);

        Page<MachineStationRegister> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<MachineStationRegister> result = page(page, wrapper);

        return result.convert(this::convertToRep);
    }

    @Override
    public MachineStationRegisterRep getMachineStationRegisterDetail(Long registerId) {
        MachineStationRegister register = getById(registerId);
        if (register == null) {
            return null;
        }

        return convertToRep(register);
    }

    @Override
    public List<MachineStationRegisterRep> getRegistersByStationId(Long stationId) {
        LambdaQueryWrapper<MachineStationRegister> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MachineStationRegister::getStationId, stationId);
        wrapper.orderByAsc(MachineStationRegister::getRegisterAddress);

        List<MachineStationRegister> registers = list(wrapper);
        return registers.stream().map(this::convertToRep).collect(Collectors.toList());
    }

    @Override
    public List<MachineStationRegisterRep> getRegistersByVertexCode(String vertexCode) {
        LambdaQueryWrapper<MachineStationRegister> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MachineStationRegister::getVertexCode, vertexCode);
        wrapper.orderByAsc(MachineStationRegister::getStationId, MachineStationRegister::getRegisterAddress);

        List<MachineStationRegister> registers = list(wrapper);
        return registers.stream().map(this::convertToRep).collect(Collectors.toList());
    }

    @Override
    public List<MachineStationRegister> getAllEnableRegister(List<Long> idList) {
        LambdaQueryWrapper<MachineStationRegister> wrapper = Wrappers.lambdaQuery();
        wrapper.in(MachineStationRegister::getStationId, idList);

        return list(wrapper);
    }

    /**
     * 转换为响应对象
     */
    private MachineStationRegisterRep convertToRep(MachineStationRegister register) {
        MachineStationRegisterRep rep = new MachineStationRegisterRep();
        BeanUtil.copyProperties(register, rep);

        // 设置寄存器类型描述
        if (register.getRegisterType() != null) {
            switch (register.getRegisterType()) {
                case 1:
                    rep.setRegisterTypeDesc("机台控制寄存器");
                    break;
                case 2:
                    rep.setRegisterTypeDesc("请求进入寄存器");
                    break;
                case 3:
                    rep.setRegisterTypeDesc("允许进入寄存器");
                    break;
                case 4:
                    rep.setRegisterTypeDesc("AGV退出完成寄存器");
                    break;
                default:
                    rep.setRegisterTypeDesc("未知");
                    break;
            }
        }

        // 设置状态描述
        if (register.getStatus() != null) {
            rep.setStatusDesc(register.getStatus() == 1 ? "启用" : "禁用");
        }

        return rep;
    }
}
